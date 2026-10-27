package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlayerProfile
import com.example.data.model.QuestStatus
import com.example.data.model.XpTransaction
import com.example.domain.AttributeStat
import com.example.domain.LevelCalculator
import com.example.domain.LevelProgress
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.LevelProgressBar
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CharacterScreen(
    viewModel: MainViewModel
) {
    val profile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val progress by viewModel.levelProgress.collectAsStateWithLifecycle()
    val attributes by viewModel.characterAttributes.collectAsStateWithLifecycle()
    val transactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val allQuests by viewModel.allQuests.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    val completedQuests = remember(allQuests) {
        allQuests.filter { it.status == QuestStatus.COMPLETED }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Character Hero Card
        item {
            CharacterHeroCard(
                profile = profile,
                progress = progress,
                onEditClick = { showEditProfileDialog = true }
            )
        }

        // Attributes Detailed Breakdown
        item {
            Text(
                text = "CHARACTER ATTRIBUTES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                letterSpacing = 1.sp
            )
        }

        items(attributes, key = { it.code }) { stat ->
            AttributeDetailCard(stat = stat)
        }

        // Lifetime Combat Records
        item {
            Text(
                text = "LIFETIME RPG METRICS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = LegendaryGold,
                letterSpacing = 1.sp
            )
        }

        item {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    MetricRow(label = "Total Quests Completed", value = "${completedQuests.size}")
                    MetricRow(label = "Current Streak Record", value = "${profile.currentStreak} Days")
                    MetricRow(label = "Best Lifetime Streak", value = "${profile.bestStreak} Days")
                    MetricRow(label = "Total Lifetime XP", value = "${profile.lifetimeXp} XP")
                    val maxSingleXp = completedQuests.maxOfOrNull { it.xpReward } ?: 0
                    MetricRow(label = "Highest Single Quest Reward", value = "+$maxSingleXp XP")
                }
            }
        }

        // XP Transaction History Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "XP TRANSACTION HISTORY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No XP transactions recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionItemCard(tx = tx)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = profile.name,
            currentTitle = profile.title,
            currentAvatarId = profile.avatarId,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, title, avatarId ->
                viewModel.updatePlayerIdentity(name, title, avatarId)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun CharacterHeroCard(
    profile: PlayerProfile,
    progress: LevelProgress,
    onEditClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("character_hero_card"),
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Avatar Crest
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CyberPurple.copy(alpha = 0.25f))
                    .border(BorderStroke(2.dp, NeonCyan), CircleShape)
                    .clickable { onEditClick() }
                    .testTag("character_avatar_large"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp).testTag("edit_player_button")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = profile.title,
                style = MaterialTheme.typography.titleSmall,
                color = LegendaryGold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rank Badge
            CyberBadge(
                text = "RANK ${progress.rank.tier} • ${progress.rank.title}",
                color = progress.rank.composeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            LevelProgressBar(progress = progress)
        }
    }
}

@Composable
fun AttributeDetailCard(stat: AttributeStat) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(stat.composeColor.copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, stat.composeColor), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stat.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = stat.composeColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stat.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CyberBadge(
                    text = "LEVEL ${stat.level}",
                    color = stat.composeColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Attribute XP: ${stat.totalXp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TransactionItemCard(tx: XpTransaction) {
    val dateStr = remember(tx.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LegendaryGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = LegendaryGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.questTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "+${tx.xpEarned} XP",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentTitle: String,
    currentAvatarId: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var title by remember { mutableStateOf(currentTitle) }
    var selectedAvatar by remember { mutableStateOf(currentAvatarId) }

    val presetTitles = listOf(
        "Novice Adventurer",
        "Vanguard Operative",
        "Apex Strategist",
        "Cyber Grandmaster",
        "Sovereign Mind"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "PLAYER IDENTITY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Codename") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_player_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Custom Title") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_player_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Or choose a preset title:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                presetTitles.forEach { preset ->
                    Text(
                        text = "• $preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (title == preset) NeonCyan else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { title = preset }
                            .padding(vertical = 3.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), title.trim(), selectedAvatar)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("save_player_identity_button")
            ) {
                Text("SAVE", color = Color(0xFF030712), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
