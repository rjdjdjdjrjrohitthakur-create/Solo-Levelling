package com.example.ui.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Achievement
import com.example.data.model.PlayerProfile
import com.example.data.model.Quest
import com.example.data.model.QuestCategory
import com.example.data.model.QuestStatus
import com.example.data.model.QuestType
import com.example.data.model.XpTransaction
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.RpgRepository
import com.example.domain.AttributeStat
import com.example.domain.LevelCalculator
import com.example.domain.LevelProgress
import com.example.domain.RpgRank
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LevelUpDialogData(
    val oldLevel: Int,
    val newLevel: Int,
    val rank: RpgRank,
    val nextXpRequired: Long
)

data class XpGainToastData(
    val questTitle: String,
    val xpGained: Int,
    val category: QuestCategory
)

enum class QuestSortBy {
    PRIORITY,
    DUE_DATE,
    XP_REWARD,
    NEWEST
}

class MainViewModel(
    private val repository: RpgRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isHapticsEnabled: StateFlow<Boolean> = preferencesRepository.isHapticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val geminiApiKey: StateFlow<String> = preferencesRepository.geminiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val playerProfile: StateFlow<PlayerProfile> = repository.playerProfile
        .map { it ?: PlayerProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProfile())

    val levelProgress: StateFlow<LevelProgress> = playerProfile
        .map { LevelCalculator.calculateProgress(it.lifetimeXp) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LevelCalculator.calculateProgress(0L)
        )

    val allQuests: StateFlow<List<Quest>> = repository.allQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<XpTransaction>> = repository.recentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters for Quests Screen
    val searchQuery = MutableStateFlow("")
    val filterCategory = MutableStateFlow<QuestCategory?>(null)
    val filterType = MutableStateFlow<QuestType?>(null)
    val filterStatus = MutableStateFlow<QuestStatus?>(QuestStatus.ACTIVE)
    val sortBy = MutableStateFlow(QuestSortBy.PRIORITY)

    private data class FilterParams(
        val query: String,
        val cat: QuestCategory?,
        val type: QuestType?,
        val status: QuestStatus?,
        val sort: QuestSortBy
    )

    private val filtersFlow = combine(
        searchQuery,
        filterCategory,
        filterType,
        filterStatus,
        sortBy
    ) { query, cat, type, status, sort ->
        FilterParams(query, cat, type, status, sort)
    }

    val filteredQuests: StateFlow<List<Quest>> = combine(allQuests, filtersFlow) { quests, filters ->
        var list = quests

        if (filters.status != null) {
            list = list.filter { it.status == filters.status }
        }

        if (filters.cat != null) {
            list = list.filter { it.category == filters.cat }
        }

        if (filters.type != null) {
            list = list.filter { it.type == filters.type }
        }

        if (filters.query.isNotBlank()) {
            val q = filters.query.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
        }

        when (filters.sort) {
            QuestSortBy.PRIORITY -> list.sortedWith(
                compareByDescending<Quest> { it.priority.level }
                    .thenByDescending { it.createdAt }
            )
            QuestSortBy.DUE_DATE -> list.sortedWith(
                compareBy<Quest> { it.dueDate ?: Long.MAX_VALUE }
                    .thenByDescending { it.priority.level }
            )
            QuestSortBy.XP_REWARD -> list.sortedByDescending { it.xpReward }
            QuestSortBy.NEWEST -> list.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Character Attributes calculated dynamically from all completed quests
    val characterAttributes: StateFlow<List<AttributeStat>> = allQuests.map { quests ->
        val completed = quests.filter { it.status == QuestStatus.COMPLETED }

        var strXp = 0
        var intXp = 0
        var chaXp = 0
        var wilXp = 0
        var wltXp = 0

        for (q in completed) {
            when (q.category) {
                QuestCategory.BODY_FITNESS -> strXp += q.xpReward
                QuestCategory.SKILLS_INTELLIGENCE, QuestCategory.CREATIVITY -> intXp += q.xpReward
                QuestCategory.SOCIAL_COMMUNICATION, QuestCategory.PERSONAL_FREEDOM -> chaXp += q.xpReward
                QuestCategory.MINDSET_DISCIPLINE -> wilXp += q.xpReward
                QuestCategory.MONEY_FINANCE, QuestCategory.BUSINESS_CAREER -> wltXp += q.xpReward
                QuestCategory.CUSTOM -> wilXp += q.xpReward / 2
            }
        }

        listOf(
            AttributeStat("Vitality & Power", "STR", "Body, Fitness & Endurance", strXp, LevelCalculator.getLevelFromLifetimeXp(strXp.toLong()), 0xFFEF4444),
            AttributeStat("Intelligence & Logic", "INT", "Skills, Knowledge & Strategy", intXp, LevelCalculator.getLevelFromLifetimeXp(intXp.toLong()), 0xFF00E5FF),
            AttributeStat("Willpower & Grit", "WIL", "Discipline, Focus & Mindset", wilXp, LevelCalculator.getLevelFromLifetimeXp(wilXp.toLong()), 0xFF8B5CF6),
            AttributeStat("Wealth & Capital", "WLT", "Finance, Investments & Career", wltXp, LevelCalculator.getLevelFromLifetimeXp(wltXp.toLong()), 0xFF10B981),
            AttributeStat("Charisma & Freedom", "CHA", "Social, Influence & Liberty", chaXp, LevelCalculator.getLevelFromLifetimeXp(chaXp.toLong()), 0xFFF59E0B)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily & Weekly completion counts
    val dailyQuests: StateFlow<List<Quest>> = allQuests.map { list ->
        list.filter { it.type == QuestType.DAILY }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTodayCount: StateFlow<Int> = allQuests.map { list ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        list.count { it.status == QuestStatus.COMPLETED && it.lastCompletedDate == today }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedThisWeekCount: StateFlow<Int> = allQuests.map { list ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfWeek = cal.timeInMillis
        list.count { it.status == QuestStatus.COMPLETED && (it.completedAt ?: 0L) >= startOfWeek }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Events
    private val _levelUpEvent = MutableStateFlow<LevelUpDialogData?>(null)
    val levelUpEvent: StateFlow<LevelUpDialogData?> = _levelUpEvent.asStateFlow()

    private val _xpToastEvent = MutableSharedFlow<XpGainToastData>()
    val xpToastEvent = _xpToastEvent.asSharedFlow()

    fun dismissLevelUpDialog() {
        _levelUpEvent.value = null
    }

    fun completeQuest(quest: Quest, context: Context?) {
        viewModelScope.launch {
            val result = repository.completeQuest(quest)
            if (result.xpEarned > 0) {
                _xpToastEvent.emit(
                    XpGainToastData(
                        questTitle = quest.title,
                        xpGained = result.xpEarned,
                        category = quest.category
                    )
                )

                if (isHapticsEnabled.value && context != null) {
                    triggerHapticFeedback(context, isLevelUp = result.leveledUp)
                }

                if (result.leveledUp) {
                    _levelUpEvent.value = LevelUpDialogData(
                        oldLevel = result.oldLevel,
                        newLevel = result.newLevel,
                        rank = result.rank,
                        nextXpRequired = LevelCalculator.xpRequiredForNext(result.newLevel)
                    )
                }
            }
        }
    }

    fun createQuest(quest: Quest) {
        viewModelScope.launch {
            repository.insertQuest(quest)
        }
    }

    fun insertQuests(quests: List<Quest>) {
        viewModelScope.launch {
            repository.questDao.insertQuests(quests)
        }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiApiKey(key)
        }
    }

    fun updateQuest(quest: Quest) {
        viewModelScope.launch {
            repository.updateQuest(quest)
        }
    }

    fun deleteQuest(quest: Quest) {
        viewModelScope.launch {
            repository.deleteQuest(quest)
        }
    }

    fun archiveQuest(quest: Quest) {
        viewModelScope.launch {
            repository.updateQuest(quest.copy(status = QuestStatus.ARCHIVED))
        }
    }

    fun unarchiveQuest(quest: Quest) {
        viewModelScope.launch {
            repository.updateQuest(quest.copy(status = QuestStatus.ACTIVE))
        }
    }

    fun claimAchievement(ach: Achievement, context: Context?) {
        viewModelScope.launch {
            val xpClaimed = repository.claimAchievement(ach)
            if (xpClaimed > 0) {
                _xpToastEvent.emit(
                    XpGainToastData(
                        questTitle = "Achievement: ${ach.title}",
                        xpGained = xpClaimed,
                        category = QuestCategory.CUSTOM
                    )
                )
                if (isHapticsEnabled.value && context != null) {
                    triggerHapticFeedback(context, isLevelUp = false)
                }
            }
        }
    }

    fun updatePlayerIdentity(name: String, title: String, avatarId: String) {
        viewModelScope.launch {
            repository.updatePlayerIdentity(name, title, avatarId)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    private fun triggerHapticFeedback(context: Context, isLevelUp: Boolean) {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (!v.hasVibrator()) return@let
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isLevelUp) {
                        // High energy burst vibration
                        val timings = longArrayOf(0, 100, 80, 150, 80, 300)
                        val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                        v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        // Sharp click vibration
                        v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(if (isLevelUp) 400L else 50L)
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: RpgRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
