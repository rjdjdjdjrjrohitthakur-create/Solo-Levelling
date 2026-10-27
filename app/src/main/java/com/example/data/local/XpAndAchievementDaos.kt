package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Achievement
import com.example.data.model.XpTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface XpTransactionDao {
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactions(): Flow<List<XpTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: XpTransaction): Long

    @Query("SELECT COUNT(*) FROM xp_transactions WHERE questId = :questId")
    suspend fun countTransactionsForQuest(questId: Long): Int

    @Query("DELETE FROM xp_transactions")
    suspend fun deleteAllTransactions()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, xpBonus DESC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC LIMIT 5")
    fun getRecentUnlockedAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("SELECT * FROM achievements WHERE code = :code LIMIT 1")
    suspend fun getAchievementByCode(code: String): Achievement?

    @Query("UPDATE achievements SET currentValue = :value, isUnlocked = :unlocked, unlockedAt = :unlockedAt WHERE code = :code")
    suspend fun updateAchievementProgress(code: String, value: Int, unlocked: Boolean, unlockedAt: Long?)

    @Query("UPDATE achievements SET isClaimed = 1 WHERE code = :code")
    suspend fun markClaimed(code: String)

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()
}
