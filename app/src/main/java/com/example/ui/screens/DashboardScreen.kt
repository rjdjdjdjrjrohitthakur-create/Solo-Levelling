package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlayerProfile
import com.example.data.model.Quest
import com.example.domain.AttributeStat
import com.example.domain.LevelProgress
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.LevelProgressBar
import com.example.ui.components.QuestCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToQuests: () -> Unit,
    onNavigateToCharacter: () -> Unit,
    onCreateQuest: () -> Unit,
    onEditQuest: (Quest) -> Unit,
    onOpenAiForge: () -> Unit,
    onOpenApiKeyPanel: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val progress by viewModel.levelProgress.collectAsStateWithLifecycle()
    val allQuests by viewModel.allQuests.collectAsStateWithLifecycle()
    val attributes by viewModel.characterAttributes.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val completedToday by viewModel.completedTodayCount.collectAsStateWithLifecycle()
    val completedThisWeek by viewModel.completedThisWeekCount.collectAsStateWithLifecycle()
    val apiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()

    val activeDailyQuests = remember(allQuests) {
        allQuests.filter { it.type == com.example.data.model.QuestType.DAILY && it.status == com.example.data.model.QuestStatus.ACTIVE }
    }
    val activeQuests = remember(allQuests) {
        allQuests.filter { it.status == com.example.data.model.QuestStatus.ACTIVE }
    }
    val recentAchievements = remember(achievements) {
        achievements.filter { it.isUnlocked }.take(3)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. PLAYER HUD HERO CARD
            item {
                PlayerHudCard(
                    profile = profile,
                    progress = progress,
                    onAvatarClick = onNavigateToCharacter
                )
            }

            // 2. EVOLVING HUMANOID PHYSIQUE (Frail at Lvl 1 -> Muscular Titan as Lvl increases)
            item {
                com.example.ui.components.EvolvingPhysiqueCard(
                    playerLevel = progress.level
                )
            }

            // 3. AI QUEST FORGE BANNER (Goal to Quests generator)
            item {
                AiQuestForgeDashboardBanner(
                    hasApiKey = apiKey.isNotBlank(),
                    onOpenForge = onOpenAiForge,
                    onOpenApiKeyPanel = onOpenApiKeyPanel
                )
            }

            // 4. QUICK STATS ROW
            item {
                QuickStatsRow(
                    currentStreak = profile.currentStreak,
                    completedToday = completedToday,
                    completedThisWeek = completedThisWeek,
                    lifetimeXp = profile.lifetimeXp
                )
            }

            // 5. CHARACTER ATTRIBUTES PREVIEW
            item {
                AttributesPreviewCard(
                    attributes = attributes,
                    onClick = onNavigateToCharacter
                )
            }

            // 4. ACTIVE QUESTS SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE QUESTS (${activeQuests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onNavigateToQuests,
                        modifier = Modifier.testTag("view_all_quests_button")
                    ) {
                        Text(
                            text = "VIEW ALL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }

            if (activeQuests.isEmpty()) {
                item {
                    EmptyQuestPlaceholder(onCreateQuest = onCreateQuest)
                }
            } else {
                items(activeQuests.take(5), key = { it.id }) { quest ->
                    QuestCard(
                        quest = quest,
                        onComplete = { viewModel.completeQuest(it, context) },
                        onEdit = onEditQuest,
                        onDelete = { viewModel.deleteQuest(it) },
                        onArchive = { viewModel.archiveQuest(it) }
                    )
                }
            }

            // 5. RECENT ACHIEVEMENTS
            if (recentAchievements.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RECENT ACHIEVEMENTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = LegendaryGold,
                        letterSpacing = 1.sp
                    )
                }

                items(recentAchievements, key = { it.code }) { ach ->
                    RecentAchievementCard(achievement = ach)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Quick Create Floating Action Button
        FloatingActionButton(
            onClick = onCreateQuest,
            containerColor = NeonCyan,
            contentColor = Color(0xFF030712),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("dashboard_create_quest_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Quest")
        }
    }
}

@Composable
fun PlayerHudCard(
    profile: PlayerProfile,
    progress: LevelProgress,
    onAvatarClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_hud_card"),
        borderColor = NeonCyan.copy(alpha = 0.5f),
        borderWidth = 1.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Glowing border
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(CyberPurple.copy(alpha = 0.2f))
                        .border(BorderStroke(2.dp, NeonCyan), CircleShape)
                        .clickable { onAvatarClick() }
                        .testTag("player_avatar_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = NeonCyan.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onAvatarClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = profile.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LegendaryGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Lifetime XP Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LegendaryGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, LegendaryGold.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIFETIME XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = LegendaryGold
                        )
                        Text(
                            text = "${progress.lifetimeXp}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = LegendaryGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Level Progress Bar
            LevelProgressBar(progress = progress)
        }
    }
}

@Composable
fun QuickStatsRow(
    currentStreak: Int,
    completedToday: Int,
    completedThisWeek: Int,
    lifetimeXp: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Streak Card
        StatBox(
            modifier = Modifier.weight(1f),
            title = "STREAK",
            value = "${currentStreak}d",
            icon = Icons.Default.LocalFireDepartment,
            accentColor = Color(0xFFFF5722)
        )

        // Today Card
        StatBox(
            modifier = Modifier.weight(1f),
            title = "TODAY",
            value = "$completedToday",
            icon = Icons.Default.CheckCircle,
            accentColor = EnergyGreen
        )

        // Weekly Card
        StatBox(
            modifier = Modifier.weight(1f),
            title = "WEEKLY",
            value = "$completedThisWeek",
            icon = Icons.Default.CalendarToday,
            accentColor = NeonCyan
        )
    }
}

@Composable
fun StatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttributesPreviewCard(
    attributes: List<AttributeStat>,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("attributes_preview_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RPG ATTRIBUTES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "EXPAND >",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                attributes.forEach { stat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = stat.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stat.composeColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(stat.composeColor.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, stat.composeColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L${stat.level}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = stat.composeColor
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stat.totalXp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentAchievementCard(achievement: com.example.data.model.Achievement) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LegendaryGold.copy(alpha = 0.2f))
                    .border(BorderStroke(1.5.dp, LegendaryGold), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = LegendaryGold,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CyberBadge(
                text = "+${achievement.xpBonus} XP",
                color = LegendaryGold
            )
        }
    }
}

@Composable
fun EmptyQuestPlaceholder(onCreateQuest: () -> Unit) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateQuest() }
            .testTag("empty_quest_placeholder")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NO ACTIVE QUESTS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Accept a new quest to earn XP and level up your real life.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            CyberBadge(text = "+ CREATE QUEST", color = NeonCyan)
        }
    }
}

@Composable
fun AiQuestForgeDashboardBanner(
    hasApiKey: Boolean,
    onOpenForge: () -> Unit,
    onOpenApiKeyPanel: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenForge() }
            .testTag("ai_quest_forge_dashboard_banner"),
        borderColor = NeonCyan.copy(alpha = 0.6f),
        borderWidth = 1.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0F1B36),
                            Color(0xFF131131),
                            Color(0xFF0D1B2A)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(BorderStroke(1.5.dp, NeonCyan), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI QUEST FORGE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CyberBadge(
                            text = if (hasApiKey) "ONLINE" else "SMART MODE",
                            color = if (hasApiKey) EnergyGreen else LegendaryGold
                        )
                    }
                    Text(
                        text = "Tell your real-world goal -> AI creates RPG questline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan,
                    modifier = Modifier
                        .clickable { onOpenForge() }
                        .testTag("summon_quests_banner_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FORGE QUESTS FROM GOAL >",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF030712)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onOpenApiKeyPanel) {
                    Text(
                        text = if (hasApiKey) "Google Key: Set" else "Set Gemini Key",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasApiKey) EnergyGreen else LegendaryGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

