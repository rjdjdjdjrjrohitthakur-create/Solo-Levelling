package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfile)

    @Update
    suspend fun updateProfile(profile: PlayerProfile)

    @Query("UPDATE player_profile SET lifetimeXp = lifetimeXp + :xpGain WHERE id = 1")
    suspend fun addLifetimeXp(xpGain: Int)

    @Query("UPDATE player_profile SET name = :name, title = :title, avatarId = :avatarId WHERE id = 1")
    suspend fun updatePlayerIdentity(name: String, title: String, avatarId: String)

    @Query("UPDATE player_profile SET currentStreak = :streak, bestStreak = :bestStreak, lastActiveDate = :todayDate WHERE id = 1")
    suspend fun updateStreak(streak: Int, bestStreak: Int, todayDate: String)
}
