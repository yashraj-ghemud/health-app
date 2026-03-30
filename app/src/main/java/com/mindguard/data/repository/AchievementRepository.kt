package com.mindguard.data.repository

import com.mindguard.data.db.AchievementDao
import com.mindguard.data.model.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    
    fun getAllAchievements(): Flow<List<Achievement>> = achievementDao.getAllAchievements()
    
    suspend fun getAchievementByKey(key: String): Achievement? {
        return achievementDao.getAchievementByKey(key)
    }
    
    fun getUnlockedAchievements(): Flow<List<Achievement>> = achievementDao.getUnlockedAchievements()
    
    suspend fun getLockedAchievements(): List<Achievement> {
        return achievementDao.getLockedAchievements()
    }
    
    suspend fun getNewAchievements(): List<Achievement> {
        return achievementDao.getNewAchievements()
    }
    
    suspend fun getUnlockedCount(): Int {
        return achievementDao.getUnlockedCount()
    }
    
    suspend fun insertAchievement(achievement: Achievement) {
        achievementDao.insertAchievement(achievement)
    }
    
    suspend fun insertAchievements(achievements: List<Achievement>) {
        achievementDao.insertAchievements(achievements)
    }
    
    suspend fun updateAchievement(achievement: Achievement) {
        achievementDao.updateAchievement(achievement)
    }
    
    suspend fun unlockAchievement(key: String, timestamp: Long = System.currentTimeMillis()) {
        achievementDao.unlockAchievement(key, timestamp)
    }
    
    suspend fun markAllAsViewed() {
        achievementDao.markAllAsViewed()
    }
    
    suspend fun markAsViewed(key: String) {
        achievementDao.markAsViewed(key)
    }
    
    fun getAchievementProgress(): Flow<Pair<Int, Int>> {
        return getAllAchievements().map { achievements ->
            val total = achievements.size
            val unlocked = achievements.count { it.unlockedAt != null }
            Pair(unlocked, total)
        }
    }
    
    suspend fun isAchievementUnlocked(key: String): Boolean {
        return getAchievementByKey(key)?.unlockedAt != null
    }
}
