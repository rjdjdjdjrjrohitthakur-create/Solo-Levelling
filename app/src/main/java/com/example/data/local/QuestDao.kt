package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Quest
import com.example.data.model.QuestStatus
import com.example.data.model.QuestType
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY createdAt DESC")
    fun getAllQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE status = :status ORDER BY priority DESC, createdAt DESC")
    fun getQuestsByStatus(status: QuestStatus): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE type = :type AND status != 'ARCHIVED' ORDER BY priority DESC, createdAt DESC")
    fun getQuestsByType(type: QuestType): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE id = :id LIMIT 1")
    suspend fun getQuestById(id: Long): Quest?

    @Query("SELECT * FROM quests WHERE status = 'ACTIVE' ORDER BY priority DESC, createdAt DESC")
    fun getActiveQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE type = 'DAILY' AND status = 'ACTIVE'")
    fun getActiveDailyQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedQuests(): Flow<List<Quest>>

    @Query("SELECT COUNT(*) FROM quests WHERE status = 'COMPLETED'")
    fun getCompletedQuestsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM quests WHERE status = 'COMPLETED' AND completedAt >= :startOfDayTimestamp")
    fun getCompletedTodayCount(startOfDayTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<Quest>)

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    @Query("DELETE FROM quests WHERE id = :id")
    suspend fun deleteQuestById(id: Long)

    @Query("DELETE FROM quests")
    suspend fun deleteAllQuests()
}
