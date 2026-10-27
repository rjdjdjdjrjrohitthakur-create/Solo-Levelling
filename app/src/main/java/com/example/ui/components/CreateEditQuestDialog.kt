package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.Priority
import com.example.data.model.Quest
import com.example.data.model.QuestCategory
import com.example.data.model.QuestType
import com.example.data.model.Subtask
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditQuestSheet(
    existingQuest: Quest? = null,
    onDismiss: () -> Unit,
    onSave: (Quest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(existingQuest?.title ?: "") }
    var description by remember { mutableStateOf(existingQuest?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(existingQuest?.category ?: QuestCategory.SKILLS_INTELLIGENCE) }
    var selectedDifficulty by remember { mutableStateOf(existingQuest?.difficulty ?: Difficulty.MEDIUM) }
    var xpReward by remember { mutableIntStateOf(existingQuest?.xpReward ?: Difficulty.MEDIUM.defaultXp) }
    var customXpText by remember { mutableStateOf(xpReward.toString()) }
    var isCustomXp by remember { mutableStateOf(existingQuest != null && existingQuest.xpReward != existingQuest.difficulty.defaultXp) }
    var selectedType by remember { mutableStateOf(existingQuest?.type ?: QuestType.DAILY) }
    var selectedPriority by remember { mutableStateOf(existingQuest?.priority ?: Priority.MEDIUM) }

    val subtasks = remember {
        mutableStateListOf<Subtask>().apply {
            if (existingQuest != null && existingQuest.subtasksJson.isNotBlank() && existingQuest.subtasksJson != "[]") {
                val raw = existingQuest.subtasksJson.removeSurrounding("[", "]")
                if (raw.isNotBlank()) {
                    raw.split(";;").forEach { item ->
                        val parts = item.split("::")
                        if (parts.size >= 2) add(Subtask(parts[0], parts[1], parts.getOrNull(2) == "true"))
                    }
                }
            }
        }
    }

    var newSubtaskText by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("create_edit_quest_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingQuest == null) "NEW QUEST ORDER" else "UPDATE QUEST",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_quest_sheet_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quest Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text("Quest Title") },
                placeholder = { Text("e.g. Master Coroutines & Flow") },
                isError = titleError,
                supportingText = if (titleError) { { Text("Title cannot be empty") } } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quest_title_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedLabelColor = NeonCyan
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quest Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Objectives (Optional)") },
                placeholder = { Text("Details, resources, or target outcome...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("quest_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedLabelColor = NeonCyan
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(
                text = "RPG CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = cat }
                            .testTag("category_chip_${cat.name}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) cat.composeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) cat.composeColor else CyberBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) cat.composeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) cat.composeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty & XP Reward
            Text(
                text = "DIFFICULTY & XP REWARD",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.values().forEach { diff ->
                    val isSelected = selectedDifficulty == diff && !isCustomXp
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedDifficulty = diff
                                isCustomXp = false
                                xpReward = diff.defaultXp
                                customXpText = diff.defaultXp.toString()
                            }
                            .testTag("difficulty_chip_${diff.name}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) diff.composeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) diff.composeColor else CyberBorder
                        )
                    ) {
                        Text(
                            text = "${diff.displayName} (+${diff.defaultXp} XP)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) diff.composeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom XP Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customXpText,
                    onValueChange = {
                        customXpText = it
                        val parsed = it.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            xpReward = parsed
                            isCustomXp = true
                        }
                    },
                    label = { Text("Custom XP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(150.dp).testTag("custom_xp_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegendaryGold,
                        focusedLabelColor = LegendaryGold
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Total Reward: +$xpReward XP",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LegendaryGold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quest Cadence / Type
            Text(
                text = "QUEST TYPE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestType.values().forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedType = type }
                            .testTag("type_chip_${type.name}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) NeonCyan else CyberBorder
                        )
                    ) {
                        Text(
                            text = type.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority
            Text(
                text = "PRIORITY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.values().forEach { priority ->
                    val isSelected = selectedPriority == priority
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedPriority = priority }
                            .testTag("priority_chip_${priority.name}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) priority.composeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) priority.composeColor else CyberBorder
                        )
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = priority.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) priority.composeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtasks Builder
            Text(
                text = "SUBTASKS (${subtasks.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = { Text("Add sub-objective...") },
                    modifier = Modifier.weight(1f).testTag("new_subtask_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newSubtaskText.isNotBlank()) {
                            subtasks.add(
                                Subtask(
                                    id = System.currentTimeMillis().toString(),
                                    title = newSubtaskText.trim(),
                                    isCompleted = false
                                )
                            )
                            newSubtaskText = ""
                        }
                    },
                    modifier = Modifier.testTag("add_subtask_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Subtask",
                        tint = NeonCyan
                    )
                }
            }

            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                subtasks.forEachIndexed { index, subtask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. ${subtask.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { subtasks.removeAt(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Subtask",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Quest Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }

                    // Format subtasks string
                    val subtasksSerialized = if (subtasks.isEmpty()) "[]"
                    else "[" + subtasks.joinToString(";;") { "${it.id}::${it.title}::${it.isCompleted}" } + "]"

                    val questToSave = (existingQuest ?: Quest(
                        title = title.trim(),
                        category = selectedCategory,
                        difficulty = selectedDifficulty,
                        xpReward = xpReward
                    )).copy(
                        title = title.trim(),
                        description = description.trim(),
                        category = selectedCategory,
                        difficulty = selectedDifficulty,
                        xpReward = xpReward,
                        type = selectedType,
                        priority = selectedPriority,
                        subtasksJson = subtasksSerialized
                    )

                    onSave(questToSave)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_quest_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(
                    text = if (existingQuest == null) "INITIALIZE QUEST" else "UPDATE QUEST",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF030712),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
