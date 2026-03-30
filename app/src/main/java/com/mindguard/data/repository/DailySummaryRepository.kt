package com.mindguard.data.repository

import com.mindguard.data.db.DailySummaryDao
import com.mindguard.data.model.DailySummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailySummaryRepository @Inject constructor(
    private val dailySummaryDao: DailySummaryDao
) {
    
    fun getAllSummaries(): Flow<List<DailySummary>> = dailySummaryDao.getAllSummaries()
    
    suspend fun getSummaryForDate(date: String): DailySummary? {
        return dailySummaryDao.getSummaryForDate(date)
    }
    
    suspend fun getSummariesForDateRange(startDate: String, endDate: String): List<DailySummary> {
        return dailySummaryDao.getSummariesForDateRange(startDate, endDate)
    }
    
    fun getRecentSummaries(limit: Int = 7): Flow<List<DailySummary>> {
        return dailySummaryDao.getRecentSummaries(limit)
    }
    
    suspend fun getBestStreak(): Int {
        return dailySummaryDao.getBestStreak() ?: 0
    }
    
    suspend fun getCurrentStreak(date: String): Int {
        return dailySummaryDao.getCurrentStreak(date) ?: 0
    }
    
    suspend fun insertSummary(summary: DailySummary) {
        dailySummaryDao.insertSummary(summary)
    }
    
    suspend fun updateSummary(summary: DailySummary) {
        dailySummaryDao.updateSummary(summary)
    }
    
    suspend fun upsertSummary(summary: DailySummary) {
        val existing = getSummaryForDate(summary.date)
        if (existing != null) {
            updateSummary(summary)
        } else {
            insertSummary(summary)
        }
    }
    
    suspend fun deleteOldSummaries(cutoffDate: String) {
        dailySummaryDao.deleteOldSummaries(cutoffDate)
    }
    
    suspend fun getAverageWellnessScore(startDate: String, endDate: String): Double {
        return dailySummaryDao.getAverageWellnessScore(startDate, endDate) ?: 0.0
    }
    
    suspend fun getWeeklyAverage(): Double {
        val (startDate, endDate) = getDateRangeForLastDays(7)
        return getAverageWellnessScore(startDate, endDate)
    }
    
    suspend fun getMonthlyAverage(): Double {
        val (startDate, endDate) = getDateRangeForLastDays(30)
        return getAverageWellnessScore(startDate, endDate)
    }
    
    private fun getDateRangeForLastDays(days: Int): Pair<String, String> {
        val calendar = java.util.Calendar.getInstance()
        val endDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(calendar.time)
        
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days + 1)
        val startDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(calendar.time)
        
        return Pair(startDate, endDate)
    }
}
