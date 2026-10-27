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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiQuestService
import com.example.data.model.Quest
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiQuestForgeSheet(
    apiKey: String,
    onDismiss: () -> Unit,
    onAcceptQuests: (List<Quest>) -> Unit,
    onOpenApiKeyPanel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { AiQuestService() }

    var goalText by remember { mutableStateOf("") }
    var selectedTimeframe by remember { mutableStateOf("30 Days Questline") }
    var selectedIntensity by remember { mutableStateOf("Balanced Hero") }

    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val generatedQuests = remember { mutableStateListOf<Quest>() }

    val quickGoals = listOf(
        "Run 5K & Build Lean Muscle",
        "Master Android & Ship 3 Apps",
        "Save $5,000 Emergency Fund",
        "Build a $3,000/mo Side Business",
        "Cold Shower & 90-Min Flow State"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("ai_quest_forge_sheet")
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, NeonCyan), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "AI QUEST FORGE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Deconstruct Any Real Goal into RPG Quests",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_ai_forge_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // API Key Status Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (apiKey.isNotBlank()) NeonCyan.copy(alpha = 0.1f) else LegendaryGold.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, if (apiKey.isNotBlank()) NeonCyan.copy(alpha = 0.4f) else LegendaryGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (apiKey.isNotBlank()) NeonCyan else LegendaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (apiKey.isNotBlank()) "Google Gemini Neural Engine: ACTIVE" else "API Key: Not Set (Using Smart Offline Engine)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (apiKey.isNotBlank()) NeonCyan else LegendaryGold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenApiKeyPanel) {
                        Text(
                            text = if (apiKey.isNotBlank()) "EDIT" else "SET KEY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (apiKey.isNotBlank()) NeonCyan else LegendaryGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Input
            Text(
                text = "YOUR REAL-WORLD GOAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                placeholder = { Text("e.g. Lose 6kg fat, build broad shoulders and run 5k") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .testTag("ai_goal_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberBorder
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Suggestions
            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickGoals.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CyberBorder),
                        modifier = Modifier
                            .clickable { goalText = preset }
                            .testTag("goal_preset_${preset.take(6)}")
                    ) {
                        Text(
                            text = "+ $preset",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeframe Chips
            Text(
                text = "TIMEFRAME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7 Days Sprint", "30 Days Questline", "90 Days Epic").forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedTimeframe = tf },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) NeonCyan else CyberBorder)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = tf,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Intensity Chips
            Text(
                text = "DIFFICULTY INTENSITY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Casual Adventurer", "Balanced Hero", "Hardcore Apex").forEach { int ->
                    val isSelected = selectedIntensity == int
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedIntensity = int },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) LegendaryGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) LegendaryGold else CyberBorder)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = int,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LegendaryGold else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Summon AI Button
            Button(
                onClick = {
                    if (goalText.isNotBlank()) {
                        isGenerating = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = aiService.generateQuestsFromGoal(
                                apiKey = apiKey,
                                goal = goalText.trim(),
                                timeframe = selectedTimeframe,
                                intensity = selectedIntensity
                            )
                            isGenerating = false
                            res.onSuccess { quests ->
                                generatedQuests.clear()
                                generatedQuests.addAll(quests)
                            }.onFailure { err ->
                                errorMessage = err.message
                            }
                        }
                    }
                },
                enabled = goalText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("summon_ai_quests_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF030712),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("SYNTHESIZING QUESTS...", fontWeight = FontWeight.Black, color = Color(0xFF030712))
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF030712))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SUMMON AI QUESTS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF030712),
                        letterSpacing = 1.sp
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444)
                )
            }

            // Results Section
            if (generatedQuests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GENERATED QUESTLINE (${generatedQuests.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    val totalXp = generatedQuests.sumOf { it.xpReward }
                    CyberBadge(text = "+$totalXp TOTAL XP", color = LegendaryGold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                generatedQuests.forEachIndexed { index, quest ->
                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${index + 1}. ${quest.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                CyberBadge(text = "+${quest.xpReward} XP", color = LegendaryGold)
                            }
                            if (quest.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = quest.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CyberBadge(text = quest.category.displayName, color = quest.category.composeColor)
                                CyberBadge(text = quest.difficulty.displayName, color = quest.difficulty.composeColor)
                                CyberBadge(text = quest.type.displayName, color = NeonCyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onAcceptQuests(generatedQuests.toList())
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("accept_all_ai_quests_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF030712))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACCEPT ALL & INJECT INTO JOURNAL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF030712)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
