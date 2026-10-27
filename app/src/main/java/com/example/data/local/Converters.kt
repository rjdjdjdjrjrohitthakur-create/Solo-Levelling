package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Difficulty
import com.example.data.model.Priority
import com.example.data.model.QuestCategory
import com.example.data.model.QuestStatus
import com.example.data.model.QuestType

class Converters {
    @TypeConverter
    fun fromCategory(value: QuestCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): QuestCategory = runCatching {
        QuestCategory.valueOf(value)
    }.getOrDefault(QuestCategory.CUSTOM)

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = runCatching {
        Difficulty.valueOf(value)
    }.getOrDefault(Difficulty.MEDIUM)

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = runCatching {
        Priority.valueOf(value)
    }.getOrDefault(Priority.MEDIUM)

    @TypeConverter
    fun fromQuestType(value: QuestType): String = value.name

    @TypeConverter
    fun toQuestType(value: String): QuestType = runCatching {
        QuestType.valueOf(value)
    }.getOrDefault(QuestType.DAILY)

    @TypeConverter
    fun fromStatus(value: QuestStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): QuestStatus = runCatching {
        QuestStatus.valueOf(value)
    }.getOrDefault(QuestStatus.ACTIVE)
}
