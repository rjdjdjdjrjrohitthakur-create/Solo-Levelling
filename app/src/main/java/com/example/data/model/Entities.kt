package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Player 1",
    val title: String = "Novice Adventurer",
    val avatarId: String = "cyber_warrior",
    val lifetimeXp: Long = 0L,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "xp_transactions")
data class XpTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val questId: Long,
    val questTitle: String,
    val xpEarned: Int,
    val category: QuestCategory,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val code: String,
    val title: String,
    val description: String,
    val xpBonus: Int,
    val iconName: String,
    val categoryTag: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val isClaimed: Boolean = false
)
