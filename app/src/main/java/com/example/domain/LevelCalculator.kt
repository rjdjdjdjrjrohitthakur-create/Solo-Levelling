package com.example.domain

import androidx.compose.ui.graphics.Color
import kotlin.math.floor
import kotlin.math.sqrt

data class RpgRank(
    val tier: String,
    val title: String,
    val badgeColorHex: Long
) {
    val composeColor: Color
        get() = Color(badgeColorHex)
}

data class LevelProgress(
    val level: Int,
    val lifetimeXp: Long,
    val currentLevelXp: Long,
    val xpRequiredForNextLevel: Long,
    val xpRemainingToNextLevel: Long,
    val progressFraction: Float,
    val rank: RpgRank
)

data class AttributeStat(
    val name: String,
    val code: String,
    val description: String,
    val totalXp: Int,
    val level: Int,
    val colorHex: Long
) {
    val composeColor: Color
        get() = Color(colorHex)
}

object LevelCalculator {

    /**
     * Cumulative XP required to reach level L from level 1.
     * Level 1 = 0 XP
     * Level 2 = 100 XP
     * Level 3 = 100 + 200 = 300 XP
     * Formula: 50 * (L - 1) * L
     */
    fun cumulativeXpForLevel(level: Int): Long {
        if (level <= 1) return 0L
        val l = level.toLong()
        return 50L * (l - 1L) * l
    }

    /**
     * XP required to progress from level L to level L + 1.
     * Formula: 100 * L
     */
    fun xpRequiredForNext(level: Int): Long {
        if (level < 1) return 100L
        return 100L * level
    }

    /**
     * Calculate level from lifetime cumulative XP.
     */
    fun getLevelFromLifetimeXp(lifetimeXp: Long): Int {
        if (lifetimeXp <= 0L) return 1
        // L^2 - L - (lifetimeXp / 50) = 0
        // L = floor((1 + sqrt(1 + 4 * (lifetimeXp / 50))) / 2)
        val d = 1.0 + (4.0 * lifetimeXp.toDouble() / 50.0)
        val calculated = floor((1.0 + sqrt(d)) / 2.0).toInt()
        return calculated.coerceAtLeast(1)
    }

    /**
     * Get full level progress breakdown.
     */
    fun calculateProgress(lifetimeXp: Long): LevelProgress {
        val level = getLevelFromLifetimeXp(lifetimeXp)
        val baseCumulativeXp = cumulativeXpForLevel(level)
        val nextCumulativeXp = cumulativeXpForLevel(level + 1)
        val requiredForNext = xpRequiredForNext(level)

        val currentLevelXp = (lifetimeXp - baseCumulativeXp).coerceAtLeast(0L)
        val xpRemaining = (nextCumulativeXp - lifetimeXp).coerceAtLeast(0L)
        val fraction = (currentLevelXp.toFloat() / requiredForNext.toFloat()).coerceIn(0f, 1f)
        val rank = getRank(level)

        return LevelProgress(
            level = level,
            lifetimeXp = lifetimeXp,
            currentLevelXp = currentLevelXp,
            xpRequiredForNextLevel = requiredForNext,
            xpRemainingToNextLevel = xpRemaining,
            progressFraction = fraction,
            rank = rank
        )
    }

    fun getRank(level: Int): RpgRank {
        return when {
            level >= 60 -> RpgRank("EX", "Apex Legend", 0xFFFF0055)
            level >= 40 -> RpgRank("S", "Diamond Sovereign", 0xFFA855F7)
            level >= 25 -> RpgRank("A", "Platinum Paragon", 0xFF00F0FF)
            level >= 15 -> RpgRank("B", "Gold Vanguard", 0xFFF59E0B)
            level >= 10 -> RpgRank("C", "Silver Specialist", 0xFFE2E8F0)
            level >= 5 -> RpgRank("D", "Iron Adventurer", 0xFF94A3B8)
            else -> RpgRank("E", "Bronze Novice", 0xFFCD7F32)
        }
    }
}
