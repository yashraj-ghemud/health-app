package com.mindguard.data.db

import androidx.room.*
import com.mindguard.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    
    @Query("SELECT * FROM achievements ORDER BY category, title")
    fun getAllAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements ORDER BY category, title")
    suspend fun getAllAchievementsSync(): List<Achievement>
    
    @Query("SELECT * FROM achievements WHERE achievementKey = :key")
    suspend fun getAchievementByKey(key: String): Achievement?
    
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NULL")
    suspend fun getLockedAchievements(): List<Achievement>
    
    @Query("SELECT * FROM achievements WHERE isNew = 1")
    suspend fun getNewAchievements(): List<Achievement>
    
    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)
    
    @Update
    suspend fun updateAchievement(achievement: Achievement)
    
    @Query("UPDATE achievements SET unlockedAt = :timestamp, isNew = 1 WHERE achievementKey = :key")
    suspend fun unlockAchievement(key: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE achievements SET isNew = 0")
    suspend fun markAllAsViewed()
    
    @Query("UPDATE achievements SET isNew = 0 WHERE achievementKey = :key")
    suspend fun markAsViewed(key: String)
}
