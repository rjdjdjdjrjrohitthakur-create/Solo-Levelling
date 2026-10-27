package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Achievement
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AchievementsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalBonusXp = remember(achievements) { achievements.sumOf { it.xpBonus } }
    val earnedBonusXp = remember(achievements) { achievements.filter { it.isUnlocked }.sumOf { it.xpBonus } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Achievements Header Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achievements_header_card"),
                borderColor = LegendaryGold.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LegendaryGold.copy(alpha = 0.2f))
                                .border(BorderStroke(1.5.dp, LegendaryGold), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = LegendaryGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "TROPHY VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = LegendaryGold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$unlockedCount of ${achievements.size} Badges Conquered",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val progressFraction = if (achievements.isEmpty()) 0f else unlockedCount.toFloat() / achievements.size.toFloat()
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = LegendaryGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Total XP Bonus Potential: $totalBonusXp XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "$earnedBonusXp XP Earned",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LegendaryGold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "RPG MILESTONES & FEATS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                letterSpacing = 1.sp
            )
        }

        items(achievements, key = { it.code }) { ach ->
            AchievementCardItem(
                achievement = ach,
                onClaim = { viewModel.claimAchievement(ach, context) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun AchievementCardItem(
    achievement: Achievement,
    onClaim: () -> Unit
) {
    val isUnlocked = achievement.isUnlocked
    val isClaimed = achievement.isClaimed

    val borderColor = when {
        isClaimed -> EnergyGreen.copy(alpha = 0.4f)
        isUnlocked -> LegendaryGold.copy(alpha = 0.7f)
        else -> CyberBorder
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("achievement_card_${achievement.code}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trophy/Lock icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) LegendaryGold.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isUnlocked) LegendaryGold else Color.Gray.copy(alpha = 0.5f)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) LegendaryGold else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                CyberBadge(
                    text = "+${achievement.xpBonus} XP",
                    color = if (isUnlocked) LegendaryGold else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar & Claim button
            val progressFraction = (achievement.currentValue.toFloat() / achievement.targetValue.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isUnlocked) LegendaryGold else NeonCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${achievement.currentValue} / ${achievement.targetValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isUnlocked) {
                    Spacer(modifier = Modifier.width(16.dp))
                    if (!isClaimed) {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("claim_achievement_${achievement.code}")
                        ) {
                            Text(
                                text = "CLAIM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF030712)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EnergyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CLAIMED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EnergyGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
