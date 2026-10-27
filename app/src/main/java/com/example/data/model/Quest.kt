package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: QuestCategory,
    val difficulty: Difficulty,
    val xpReward: Int,
    val dueDate: Long? = null,
    val deadlineTime: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val type: QuestType = QuestType.DAILY,
    val subtasksJson: String = "[]",
    val status: QuestStatus = QuestStatus.ACTIVE,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val streak: Int = 0,
    val lastCompletedDate: String? = null
)
