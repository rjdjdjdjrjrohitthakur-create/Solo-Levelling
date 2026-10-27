package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuestCategory(
    val displayName: String,
    val attributeName: String,
    val colorHex: Long
) {
    MONEY_FINANCE("Money & Finance", "Wealth", 0xFF10B981),
    BUSINESS_CAREER("Business & Career", "Influence", 0xFF3B82F6),
    SKILLS_INTELLIGENCE("Skills & Intelligence", "Intelligence", 0xFF00E5FF),
    MINDSET_DISCIPLINE("Mindset & Discipline", "Willpower", 0xFF8B5CF6),
    BODY_FITNESS("Body & Fitness", "Vitality", 0xFFEF4444),
    SOCIAL_COMMUNICATION("Social & Communication", "Charisma", 0xFFF59E0B),
    CREATIVITY("Creativity", "Wisdom", 0xFFEC4899),
    PERSONAL_FREEDOM("Personal Freedom", "Autonomy", 0xFF06B6D4),
    CUSTOM("Custom", "Adaptability", 0xFF94A3B8);

    val composeColor: Color
        get() = Color(colorHex)

    val icon: ImageVector
        get() = when (this) {
            MONEY_FINANCE -> Icons.Default.AccountBalance
            BUSINESS_CAREER -> Icons.Default.Work
            SKILLS_INTELLIGENCE -> Icons.Default.Psychology
            MINDSET_DISCIPLINE -> Icons.Default.Bolt
            BODY_FITNESS -> Icons.Default.FitnessCenter
            SOCIAL_COMMUNICATION -> Icons.Default.RecordVoiceOver
            CREATIVITY -> Icons.Default.AutoAwesome
            PERSONAL_FREEDOM -> Icons.Default.EmojiEvents
            CUSTOM -> Icons.Default.AutoAwesome
        }
}

enum class Difficulty(val displayName: String, val defaultXp: Int, val colorHex: Long) {
    EASY("Easy", 10, 0xFF10B981),
    MEDIUM("Medium", 25, 0xFF3B82F6),
    HARD("Hard", 50, 0xFFF59E0B),
    EPIC("Epic", 100, 0xFF8B5CF6),
    LEGENDARY("Legendary", 250, 0xFFFFB703);

    val composeColor: Color
        get() = Color(colorHex)
}

enum class Priority(val displayName: String, val level: Int, val colorHex: Long) {
    LOW("Low", 1, 0xFF64748B),
    MEDIUM("Medium", 2, 0xFF38BDF8),
    HIGH("High", 3, 0xFFF59E0B),
    CRITICAL("Critical", 4, 0xFFEF4444);

    val composeColor: Color
        get() = Color(colorHex)
}

enum class QuestType(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    RECURRING("Recurring"),
    ONE_TIME("One-time"),
    LONG_TERM("Long-term")
}

enum class QuestStatus {
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)
