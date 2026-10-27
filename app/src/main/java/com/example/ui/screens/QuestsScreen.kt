package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Quest
import com.example.data.model.QuestCategory
import com.example.data.model.QuestStatus
import com.example.data.model.QuestType
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.QuestCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuestSortBy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestsScreen(
    viewModel: MainViewModel,
    onCreateQuest: () -> Unit,
    onEditQuest: (Quest) -> Unit,
    onOpenAiForge: () -> Unit
) {
    val context = LocalContext.current
    val quests by viewModel.filteredQuests.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val selectedType by viewModel.filterType.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var sortMenuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // AI Forge Quick Banner in Quests
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenAiForge() }
                        .testTag("quests_screen_ai_forge_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF101C38),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI QUEST FORGE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = "Turn real goals into questlines with Google AI",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        CyberBadge(text = "SUMMON >", color = NeonCyan)
                    }
                }
            }

            // 1. Status Tabs (Active, Completed, Archived)
            item {
                val statuses = listOf(QuestStatus.ACTIVE, QuestStatus.COMPLETED, QuestStatus.ARCHIVED)
                val selectedIndex = statuses.indexOf(selectedStatus).coerceAtLeast(0)

                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = NeonCyan
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("quest_status_tabs")
                ) {
                    statuses.forEach { status ->
                        Tab(
                            selected = selectedStatus == status,
                            onClick = { viewModel.filterStatus.value = status },
                            text = {
                                Text(
                                    text = status.name,
                                    fontWeight = if (selectedStatus == status) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // 2. Search & Sort Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search quests...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quest_search_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberBorder
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { sortMenuOpen = true }
                                .testTag("quest_sort_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = NeonCyan)
                            }
                        }

                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false }
                        ) {
                            QuestSortBy.values().forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text("Sort by ${sort.name.replace("_", " ")}") },
                                    onClick = {
                                        viewModel.sortBy.value = sort
                                        sortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Cadence Type Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.filterType.value = null }
                                .testTag("type_filter_all"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedType == null) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selectedType == null) NeonCyan else CyberBorder)
                        ) {
                            Text(
                                text = "All Types",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedType == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == null) NeonCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    items(QuestType.values()) { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.filterType.value = if (isSelected) null else type
                                }
                                .testTag("type_filter_${type.name}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else CyberBorder)
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
            }

            // 4. Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.filterCategory.value = null }
                                .testTag("category_filter_all"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == null) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selectedCategory == null) NeonCyan else CyberBorder)
                        ) {
                            Text(
                                text = "All Categories",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == null) NeonCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    items(QuestCategory.values()) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.filterCategory.value = if (isSelected) null else cat
                                }
                                .testTag("category_filter_${cat.name}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) cat.composeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) cat.composeColor else CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) cat.composeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
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
            }

            // Results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${quests.size} QUESTS FOUND",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Quest List Items
            if (quests.isEmpty()) {
                item {
                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NO QUESTS FOUND",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing your filters or create a new quest below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(quests, key = { it.id }) { quest ->
                    QuestCard(
                        quest = quest,
                        onComplete = { viewModel.completeQuest(it, context) },
                        onEdit = onEditQuest,
                        onDelete = { viewModel.deleteQuest(it) },
                        onArchive = {
                            if (quest.status == QuestStatus.ARCHIVED) viewModel.unarchiveQuest(quest)
                            else viewModel.archiveQuest(quest)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Create Quest FAB
        FloatingActionButton(
            onClick = onCreateQuest,
            containerColor = NeonCyan,
            contentColor = Color(0xFF030712),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("quests_screen_create_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Quest")
        }
    }
}
