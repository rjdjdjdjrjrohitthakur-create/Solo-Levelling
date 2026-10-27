package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Quest
import com.example.ui.components.AiQuestForgeSheet
import com.example.ui.components.ApiKeyPanelDialog
import com.example.ui.components.CreateEditQuestSheet
import com.example.ui.components.CyberBadge
import com.example.ui.components.LevelUpDialog
import com.example.ui.components.XpGainNotification
import com.example.ui.navigation.RpgBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CharacterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.XpGainToastData
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val levelProgress by viewModel.levelProgress.collectAsStateWithLifecycle()
    val levelUpData by viewModel.levelUpEvent.collectAsStateWithLifecycle()
    val apiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()

    var activeToastData by remember { mutableStateOf<XpGainToastData?>(null) }
    var questToEdit by remember { mutableStateOf<Quest?>(null) }
    var showCreateQuestSheet by remember { mutableStateOf(false) }
    var showAiForgeSheet by remember { mutableStateOf(false) }
    var showApiKeyPanelDialog by remember { mutableStateOf(false) }

    // Listen for XP toast events
    LaunchedEffect(Unit) {
        viewModel.xpToastEvent.collect { data ->
            activeToastData = data
            delay(2800)
            if (activeToastData == data) {
                activeToastData = null
            }
        }
    }

    // BackHandler: return to Dashboard if on another screen
    if (currentScreen != Screen.Dashboard) {
        BackHandler {
            currentScreen = Screen.Dashboard
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cyber RPG App Title
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LEVEL UP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                CyberBadge(
                                    text = "RPG",
                                    color = LegendaryGold
                                )
                            }
                            Text(
                                text = currentScreen.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // AI Forge Quick Action Icon
                        IconButton(
                            onClick = { showAiForgeSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)), CircleShape)
                                .testTag("topbar_ai_forge_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Quest Forge",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Compact HUD Level Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = LegendaryGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LVL ${levelProgress.level}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            RpgBottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_crossfade"
            ) { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToQuests = { currentScreen = Screen.Quests },
                        onNavigateToCharacter = { currentScreen = Screen.Character },
                        onCreateQuest = {
                            questToEdit = null
                            showCreateQuestSheet = true
                        },
                        onEditQuest = { quest ->
                            questToEdit = quest
                            showCreateQuestSheet = true
                        },
                        onOpenAiForge = { showAiForgeSheet = true },
                        onOpenApiKeyPanel = { showApiKeyPanelDialog = true }
                    )
                    Screen.Quests -> QuestsScreen(
                        viewModel = viewModel,
                        onCreateQuest = {
                            questToEdit = null
                            showCreateQuestSheet = true
                        },
                        onEditQuest = { quest ->
                            questToEdit = quest
                            showCreateQuestSheet = true
                        },
                        onOpenAiForge = { showAiForgeSheet = true }
                    )
                    Screen.Character -> CharacterScreen(
                        viewModel = viewModel
                    )
                    Screen.Achievements -> AchievementsScreen(
                        viewModel = viewModel
                    )
                    Screen.Settings -> SettingsScreen(
                        viewModel = viewModel,
                        onOpenApiKeyPanel = { showApiKeyPanelDialog = true }
                    )
                }
            }

            // Floating XP Gain Banner at top
            XpGainNotification(
                data = activeToastData,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Level-up celebration modal
    levelUpData?.let { data ->
        LevelUpDialog(
            data = data,
            onDismiss = { viewModel.dismissLevelUpDialog() }
        )
    }

    // Quest Creation / Edit Bottom Sheet
    if (showCreateQuestSheet) {
        CreateEditQuestSheet(
            existingQuest = questToEdit,
            onDismiss = {
                showCreateQuestSheet = false
                questToEdit = null
            },
            onSave = { quest ->
                if (questToEdit == null) {
                    viewModel.createQuest(quest)
                } else {
                    viewModel.updateQuest(quest)
                }
            }
        )
    }

    // AI Quest Forge Bottom Sheet
    if (showAiForgeSheet) {
        AiQuestForgeSheet(
            apiKey = apiKey,
            onDismiss = { showAiForgeSheet = false },
            onAcceptQuests = { quests ->
                viewModel.insertQuests(quests)
            },
            onOpenApiKeyPanel = {
                showApiKeyPanelDialog = true
            }
        )
    }

    // Google Gemini API Key Panel Dialog
    if (showApiKeyPanelDialog) {
        ApiKeyPanelDialog(
            currentApiKey = apiKey,
            onDismiss = { showApiKeyPanelDialog = false },
            onSaveKey = { key ->
                viewModel.setGeminiApiKey(key)
            }
        )
    }
}
