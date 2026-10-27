package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.Quest
import com.example.data.model.QuestStatus
import com.example.data.model.QuestType
import com.example.data.model.Subtask
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestCard(
    quest: Quest,
    onComplete: (Quest) -> Unit,
    onEdit: (Quest) -> Unit,
    onDelete: (Quest) -> Unit,
    onArchive: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var subtasksExpanded by remember { mutableStateOf(false) }

    val isCompleted = quest.status == QuestStatus.COMPLETED
    val borderColor = if (isCompleted) {
        EnergyGreen.copy(alpha = 0.5f)
    } else when (quest.priority) {
        Priority.CRITICAL -> Color(0xFFEF4444).copy(alpha = 0.7f)
        Priority.HIGH -> LegendaryGold.copy(alpha = 0.5f)
        else -> CyberBorder
    }

    // Parse simple subtasks JSON or comma separated
    val subtasks = remember(quest.subtasksJson) {
        runCatching {
            if (quest.subtasksJson.isBlank() || quest.subtasksJson == "[]") emptyList<Subtask>()
            else {
                // lightweight parser: items like "title|true", "title|false" or clean list
                val raw = quest.subtasksJson.removeSurrounding("[", "]")
                if (raw.isBlank()) emptyList()
                else {
                    raw.split(";;").mapNotNull { item ->
                        val parts = item.split("::")
                        if (parts.size >= 2) Subtask(parts[0], parts[1], parts.getOrNull(2) == "true")
                        else null
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quest_card_${quest.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Checkbox / Complete Circle, Title, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // RPG Checkbox Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) EnergyGreen.copy(alpha = 0.2f)
                            else NeonCyan.copy(alpha = 0.1f)
                        )
                        .border(
                            BorderStroke(
                                2.dp,
                                if (isCompleted) EnergyGreen else NeonCyan
                            ),
                            CircleShape
                        )
                        .clickable {
                            if (!isCompleted) {
                                onComplete(quest)
                            }
                        }
                        .testTag("complete_quest_button_${quest.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = EnergyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Complete Quest",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    if (quest.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quest.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp).testTag("quest_menu_${quest.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Quest") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit(quest)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (quest.status == QuestStatus.ARCHIVED) "Unarchive" else "Archive") },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onArchive(quest)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                menuExpanded = false
                                onDelete(quest)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges Flow Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Category Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = quest.category.composeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, quest.category.composeColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = quest.category.icon,
                            contentDescription = null,
                            tint = quest.category.composeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = quest.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = quest.category.composeColor
                        )
                    }
                }

                // Difficulty Badge
                CyberBadge(
                    text = quest.difficulty.displayName,
                    color = quest.difficulty.composeColor
                )

                // XP Reward
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LegendaryGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, LegendaryGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = LegendaryGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${quest.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = LegendaryGold
                        )
                    }
                }

                // Type Badge
                CyberBadge(
                    text = quest.type.displayName,
                    color = NeonCyan
                )

                // Streak Badge for daily / recurring quests
                if (quest.streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF5722).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${quest.streak}d streak",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                        }
                    }
                }

                // Due Date
                if (quest.dueDate != null) {
                    val formattedDate = remember(quest.dueDate) {
                        SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(quest.dueDate))
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Subtasks expander if any
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val completedCount = subtasks.count { it.isCompleted }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { subtasksExpanded = !subtasksExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtasks ($completedCount/${subtasks.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (subtasksExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle subtasks",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = subtasksExpanded) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subtask.isCompleted,
                                    onCheckedChange = null,
                                    modifier = Modifier.size(24.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = NeonCyan,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subtask.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
