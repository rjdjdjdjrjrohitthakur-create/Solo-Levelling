package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Achievement
import com.example.data.model.Difficulty
import com.example.data.model.PlayerProfile
import com.example.data.model.Priority
import com.example.data.model.Quest
import com.example.data.model.QuestCategory
import com.example.data.model.QuestStatus
import com.example.data.model.QuestType
import com.example.data.model.XpTransaction
import com.example.domain.LevelCalculator
import com.example.domain.RpgRank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CompletionResult(
    val xpEarned: Int,
    val leveledUp: Boolean,
    val oldLevel: Int,
    val newLevel: Int,
    val rank: RpgRank,
    val unlockedAchievements: List<Achievement>
)

class RpgRepository(private val db: AppDatabase) {
    val questDao = db.questDao()
    val profileDao = db.profileDao()
    val xpTransactionDao = db.xpTransactionDao()
    val achievementDao = db.achievementDao()

    val allQuests: Flow<List<Quest>> = questDao.getAllQuests()
    val activeQuests: Flow<List<Quest>> = questDao.getActiveQuests()
    val playerProfile: Flow<PlayerProfile?> = profileDao.getProfileFlow()
    val recentTransactions: Flow<List<XpTransaction>> = xpTransactionDao.getRecentTransactions()
    val achievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getTodayDateString(): String = dateFormat.format(Date())

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    suspend fun ensureInitialized() {
        val profile = profileDao.getProfileSync()
        if (profile == null) {
            profileDao.insertOrUpdateProfile(
                PlayerProfile(
                    id = 1,
                    name = "Apex Operator",
                    title = "Novice Adventurer",
                    avatarId = "cyber_samurai",
                    lifetimeXp = 0L,
                    currentStreak = 0,
                    bestStreak = 0,
                    lastActiveDate = ""
                )
            )
        }

        // Initialize default achievements if empty
        val existingAchievements = achievementDao.getAllAchievements().first()
        if (existingAchievements.isEmpty()) {
            val defaults = listOf(
                Achievement("ACH_FIRST_BLOOD", "First Blood", "Complete your first quest.", 50, "bolt", "starter", 1),
                Achievement("ACH_STREAK_3", "Spark of Discipline", "Achieve a 3-day quest streak.", 75, "fire", "streak", 3),
                Achievement("ACH_STREAK_7", "Unstoppable Momentum", "Maintain a 7-day quest streak.", 200, "fire", "streak", 7),
                Achievement("ACH_LEVEL_5", "Iron Climber", "Ascend to Level 5.", 100, "military_tech", "level", 5),
                Achievement("ACH_LEVEL_10", "Silver Vanguard", "Ascend to Level 10.", 250, "military_tech", "level", 10),
                Achievement("ACH_LEVEL_25", "Platinum Paragon", "Ascend to Level 25.", 500, "military_tech", "level", 25),
                Achievement("ACH_QUESTS_10", "Tenacious Operative", "Complete 10 total quests.", 120, "task", "milestone", 10),
                Achievement("ACH_QUESTS_25", "Cyber Veteran", "Complete 25 total quests.", 250, "task", "milestone", 25),
                Achievement("ACH_QUESTS_50", "Real Life Master", "Complete 50 total quests.", 500, "task", "milestone", 50),
                Achievement("ACH_FITNESS_5", "Kinetic Power", "Complete 5 Body & Fitness quests.", 100, "fitness", "attribute", 5),
                Achievement("ACH_INTEL_5", "Neural Overclock", "Complete 5 Skills & Intelligence quests.", 100, "intel", "attribute", 5),
                Achievement("ACH_MINDSET_5", "Iron Willpower", "Complete 5 Mindset & Discipline quests.", 100, "mindset", "attribute", 5),
                Achievement("ACH_WEALTH_5", "Capital Ascendant", "Complete 5 Money & Finance quests.", 100, "wealth", "attribute", 5),
                Achievement("ACH_LEGENDARY_1", "Titan Slayer", "Conquer a Legendary difficulty quest.", 250, "star", "epic", 1)
            )
            achievementDao.insertAchievements(defaults)
        }

        // Initialize starter quests if none exist
        val existingQuests = questDao.getAllQuests().first()
        if (existingQuests.isEmpty()) {
            val starterQuests = listOf(
                Quest(
                    title = "Morning Hydration & Sunlight",
                    description = "Drink 500ml of mineral water and absorb 15 minutes of direct sunlight.",
                    category = QuestCategory.BODY_FITNESS,
                    difficulty = Difficulty.EASY,
                    xpReward = 10,
                    priority = Priority.HIGH,
                    type = QuestType.DAILY
                ),
                Quest(
                    title = "Neural Overclock: Read 20 Pages",
                    description = "Read 20 pages of non-fiction, architecture, or philosophy without phone distraction.",
                    category = QuestCategory.SKILLS_INTELLIGENCE,
                    difficulty = Difficulty.MEDIUM,
                    xpReward = 25,
                    priority = Priority.MEDIUM,
                    type = QuestType.DAILY
                ),
                Quest(
                    title = "Deep Work Protocol: 90 Min Sprint",
                    description = "Enter flow state for 90 uninterrupted minutes on high-leverage work.",
                    category = QuestCategory.MINDSET_DISCIPLINE,
                    difficulty = Difficulty.HARD,
                    xpReward = 50,
                    priority = Priority.CRITICAL,
                    type = QuestType.DAILY
                ),
                Quest(
                    title = "Audit Weekly Cash Flow & Portfolio",
                    description = "Review bank transactions, track net savings, and balance investment holdings.",
                    category = QuestCategory.MONEY_FINANCE,
                    difficulty = Difficulty.HARD,
                    xpReward = 50,
                    priority = Priority.HIGH,
                    type = QuestType.WEEKLY
                ),
                Quest(
                    title = "Heavy Resistance Training Session",
                    description = "Complete scheduled compound lifts, core work, and 10 minutes stretching.",
                    category = QuestCategory.BODY_FITNESS,
                    difficulty = Difficulty.MEDIUM,
                    xpReward = 25,
                    priority = Priority.HIGH,
                    type = QuestType.DAILY
                ),
                Quest(
                    title = "Master a Transformative High-Income Skill",
                    description = "Complete an entire professional course, ship a real project, or obtain certified expertise.",
                    category = QuestCategory.BUSINESS_CAREER,
                    difficulty = Difficulty.LEGENDARY,
                    xpReward = 250,
                    priority = Priority.CRITICAL,
                    type = QuestType.LONG_TERM
                )
            )
            questDao.insertQuests(starterQuests)
        }
    }

    suspend fun completeQuest(quest: Quest): CompletionResult {
        // Prevent duplicate completions on already completed non-recurring quests
        if (quest.status == QuestStatus.COMPLETED) {
            val currentProfile = profileDao.getProfileSync() ?: PlayerProfile()
            val currentLevel = LevelCalculator.getLevelFromLifetimeXp(currentProfile.lifetimeXp)
            return CompletionResult(
                xpEarned = 0,
                leveledUp = false,
                oldLevel = currentLevel,
                newLevel = currentLevel,
                rank = LevelCalculator.getRank(currentLevel),
                unlockedAchievements = emptyList()
            )
        }

        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        val profile = profileDao.getProfileSync() ?: PlayerProfile()

        val oldLifetimeXp = profile.lifetimeXp
        val oldLevel = LevelCalculator.getLevelFromLifetimeXp(oldLifetimeXp)
        val xpGain = quest.xpReward

        // Record XP transaction in history
        xpTransactionDao.insertTransaction(
            XpTransaction(
                questId = quest.id,
                questTitle = quest.title,
                xpEarned = xpGain,
                category = quest.category,
                timestamp = System.currentTimeMillis()
            )
        )

        // Update Quest status or streak
        val newQuest = if (quest.type == QuestType.DAILY || quest.type == QuestType.RECURRING) {
            quest.copy(
                streak = quest.streak + 1,
                lastCompletedDate = today,
                status = QuestStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            )
        } else {
            quest.copy(
                status = QuestStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
                lastCompletedDate = today
            )
        }
        questDao.updateQuest(newQuest)

        // Update Profile streak
        val newStreak = when (profile.lastActiveDate) {
            today -> profile.currentStreak
            yesterday -> profile.currentStreak + 1
            "" -> 1
            else -> 1
        }
        val bestStreak = maxOf(newStreak, profile.bestStreak)

        val newLifetimeXp = oldLifetimeXp + xpGain
        val newLevel = LevelCalculator.getLevelFromLifetimeXp(newLifetimeXp)
        val newRank = LevelCalculator.getRank(newLevel)

        profileDao.insertOrUpdateProfile(
            profile.copy(
                lifetimeXp = newLifetimeXp,
                currentStreak = newStreak,
                bestStreak = bestStreak,
                lastActiveDate = today,
                title = newRank.title
            )
        )

        // Check achievements progress
        val newlyUnlocked = checkAndUnlockAchievements(
            newLifetimeXp = newLifetimeXp,
            newLevel = newLevel,
            newStreak = newStreak,
            completedQuest = quest
        )

        return CompletionResult(
            xpEarned = xpGain,
            leveledUp = newLevel > oldLevel,
            oldLevel = oldLevel,
            newLevel = newLevel,
            rank = newRank,
            unlockedAchievements = newlyUnlocked
        )
    }

    private suspend fun checkAndUnlockAchievements(
        newLifetimeXp: Long,
        newLevel: Int,
        newStreak: Int,
        completedQuest: Quest
    ): List<Achievement> {
        val allAchievements = achievementDao.getAllAchievements().first()
        val allQuestsList = questDao.getAllQuests().first()
        val completedCount = allQuestsList.count { it.status == QuestStatus.COMPLETED }
        val categoryCompletedCount = allQuestsList.count {
            it.status == QuestStatus.COMPLETED && it.category == completedQuest.category
        }

        val newlyUnlocked = mutableListOf<Achievement>()

        for (ach in allAchievements) {
            if (ach.isUnlocked) continue

            var currentValue = ach.currentValue
            var shouldUnlock = false

            when (ach.code) {
                "ACH_FIRST_BLOOD" -> {
                    currentValue = completedCount
                    if (completedCount >= 1) shouldUnlock = true
                }
                "ACH_STREAK_3" -> {
                    currentValue = newStreak
                    if (newStreak >= 3) shouldUnlock = true
                }
                "ACH_STREAK_7" -> {
                    currentValue = newStreak
                    if (newStreak >= 7) shouldUnlock = true
                }
                "ACH_LEVEL_5" -> {
                    currentValue = newLevel
                    if (newLevel >= 5) shouldUnlock = true
                }
                "ACH_LEVEL_10" -> {
                    currentValue = newLevel
                    if (newLevel >= 10) shouldUnlock = true
                }
                "ACH_LEVEL_25" -> {
                    currentValue = newLevel
                    if (newLevel >= 25) shouldUnlock = true
                }
                "ACH_QUESTS_10" -> {
                    currentValue = completedCount
                    if (completedCount >= 10) shouldUnlock = true
                }
                "ACH_QUESTS_25" -> {
                    currentValue = completedCount
                    if (completedCount >= 25) shouldUnlock = true
                }
                "ACH_QUESTS_50" -> {
                    currentValue = completedCount
                    if (completedCount >= 50) shouldUnlock = true
                }
                "ACH_FITNESS_5" -> {
                    if (completedQuest.category == QuestCategory.BODY_FITNESS) {
                        currentValue = categoryCompletedCount
                        if (categoryCompletedCount >= 5) shouldUnlock = true
                    }
                }
                "ACH_INTEL_5" -> {
                    if (completedQuest.category == QuestCategory.SKILLS_INTELLIGENCE) {
                        currentValue = categoryCompletedCount
                        if (categoryCompletedCount >= 5) shouldUnlock = true
                    }
                }
                "ACH_MINDSET_5" -> {
                    if (completedQuest.category == QuestCategory.MINDSET_DISCIPLINE) {
                        currentValue = categoryCompletedCount
                        if (categoryCompletedCount >= 5) shouldUnlock = true
                    }
                }
                "ACH_WEALTH_5" -> {
                    if (completedQuest.category == QuestCategory.MONEY_FINANCE) {
                        currentValue = categoryCompletedCount
                        if (categoryCompletedCount >= 5) shouldUnlock = true
                    }
                }
                "ACH_LEGENDARY_1" -> {
                    if (completedQuest.difficulty == Difficulty.LEGENDARY) {
                        currentValue = 1
                        shouldUnlock = true
                    }
                }
            }

            if (shouldUnlock) {
                achievementDao.updateAchievementProgress(
                    code = ach.code,
                    value = maxOf(currentValue, ach.targetValue),
                    unlocked = true,
                    unlockedAt = System.currentTimeMillis()
                )
                newlyUnlocked.add(ach.copy(isUnlocked = true, currentValue = maxOf(currentValue, ach.targetValue)))
            } else if (currentValue != ach.currentValue) {
                achievementDao.updateAchievementProgress(
                    code = ach.code,
                    value = currentValue,
                    unlocked = false,
                    unlockedAt = null
                )
            }
        }

        return newlyUnlocked
    }

    suspend fun claimAchievement(ach: Achievement): Int {
        if (!ach.isUnlocked || ach.isClaimed) return 0
        achievementDao.markClaimed(ach.code)
        profileDao.addLifetimeXp(ach.xpBonus)
        xpTransactionDao.insertTransaction(
            XpTransaction(
                questId = -1L,
                questTitle = "Achievement: ${ach.title}",
                xpEarned = ach.xpBonus,
                category = QuestCategory.CUSTOM,
                timestamp = System.currentTimeMillis()
            )
        )
        return ach.xpBonus
    }

    suspend fun insertQuest(quest: Quest): Long = questDao.insertQuest(quest)
    suspend fun updateQuest(quest: Quest) = questDao.updateQuest(quest)
    suspend fun deleteQuest(quest: Quest) = questDao.deleteQuest(quest)
    suspend fun deleteQuestById(id: Long) = questDao.deleteQuestById(id)

    suspend fun updatePlayerIdentity(name: String, title: String, avatarId: String) {
        profileDao.updatePlayerIdentity(name, title, avatarId)
    }

    suspend fun resetAllData() {
        questDao.deleteAllQuests()
        xpTransactionDao.deleteAllTransactions()
        achievementDao.deleteAllAchievements()
        profileDao.insertOrUpdateProfile(
            PlayerProfile(
                id = 1,
                name = "Apex Operator",
                title = "Novice Adventurer",
                avatarId = "cyber_samurai",
                lifetimeXp = 0L,
                currentStreak = 0,
                bestStreak = 0,
                lastActiveDate = ""
            )
        )
        ensureInitialized()
    }
}
