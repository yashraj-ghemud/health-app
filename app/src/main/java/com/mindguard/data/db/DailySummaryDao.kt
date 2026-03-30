package com.mindguard.data.db

import androidx.room.*
import com.mindguard.data.model.DailySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {
    
    @Query("SELECT * FROM daily_summary ORDER BY date DESC")
    fun getAllSummaries(): Flow<List<DailySummary>>
    
    @Query("SELECT * FROM daily_summary WHERE date = :date")
    suspend fun getSummaryForDate(date: String): DailySummary?
    
    @Query("SELECT * FROM daily_summary WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getSummariesForDateRange(startDate: String, endDate: String): List<DailySummary>
    
    @Query("SELECT * FROM daily_summary ORDER BY date DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int = 7): Flow<List<DailySummary>>
    
    @Query("SELECT * FROM daily_summary ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSummariesSync(limit: Int = 7): List<DailySummary>
    
    @Query("SELECT MAX(streakDays) FROM daily_summary")
    suspend fun getBestStreak(): Int?
    
    @Query("SELECT streakDays FROM daily_summary WHERE date = :date")
    suspend fun getCurrentStreak(date: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummary)
    
    @Update
    suspend fun updateSummary(summary: DailySummary)
    
    @Query("DELETE FROM daily_summary WHERE date < :cutoffDate")
    suspend fun deleteOldSummaries(cutoffDate: String)
    
    @Query("SELECT AVG(wellnessScore) FROM daily_summary WHERE date >= :startDate AND date <= :endDate")
    suspend fun getAverageWellnessScore(startDate: String, endDate: String): Double?
}
