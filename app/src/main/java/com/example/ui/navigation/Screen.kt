package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Quests : Screen("quests", "Quests", Icons.AutoMirrored.Filled.Assignment)
    data object Character : Screen("character", "Character", Icons.Default.Person)
    data object Achievements : Screen("achievements", "Badges", Icons.Default.EmojiEvents)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items: List<Screen>
            get() = listOf(Dashboard, Quests, Character, Achievements, Settings)
    }
}
