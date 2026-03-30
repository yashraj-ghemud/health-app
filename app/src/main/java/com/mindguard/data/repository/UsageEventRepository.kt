package com.mindguard.data.repository

import com.mindguard.data.db.UsageEventDao
import com.mindguard.data.model.UsageEvent
import com.mindguard.data.model.AppCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageEventRepository @Inject constructor(
    private val usageEventDao: UsageEventDao,
    @com.mindguard.di.ApplicationScope private val applicationScope: CoroutineScope
) {
    
    fun getEventsForDate(date: String): Flow<List<UsageEvent>> {
        return usageEventDao.getEventsForDate(date)
    }
    
    suspend fun getEventsForDateSync(date: String): List<UsageEvent> {
        return usageEventDao.getEventsForDateSync(date)
    }
    
    fun getEventsForDateRange(startDate: String, endDate: String): Flow<List<UsageEvent>> {
        return usageEventDao.getEventsForDateRange(startDate, endDate)
    }
    
    suspend fun getEventsForApp(packageName: String, date: String): List<UsageEvent> {
        return usageEventDao.getEventsForApp(packageName, date)
    }
    
    suspend fun getTimeByCategory(date: String): List<com.mindguard.data.db.CategoryTimeSummary> {
        return usageEventDao.getTimeByCategory(date)
    }
    
    suspend fun getTopAppsForDate(date: String, limit: Int = 5): List<com.mindguard.data.db.AppTimeSummary> {
        return usageEventDao.getTopAppsForDate(date, limit)
    }
    
    suspend fun getTotalScreenTime(date: String): Int {
        return usageEventDao.getTotalScreenTime(date) ?: 0
    }
    
    suspend fun getProductiveTime(date: String): Int {
        return usageEventDao.getproductiveTime(date) ?: 0
    }
    
    suspend fun getEntertainmentTime(date: String): Int {
        return usageEventDao.getEntertainmentTime(date) ?: 0
    }
    
    suspend fun getCommunicationTime(date: String): Int {
        return usageEventDao.getCommunicationTime(date) ?: 0
    }
    
    fun insertEvent(event: UsageEvent) {
        applicationScope.launch {
            usageEventDao.insertEvent(event)
        }
    }
    
    fun insertEvents(events: List<UsageEvent>) {
        applicationScope.launch {
            usageEventDao.insertEvents(events)
        }
    }
    
    suspend fun deleteOldEvents(cutoffDate: String) {
        usageEventDao.deleteOldEvents(cutoffDate)
    }
    
    suspend fun getEventCountForDate(date: String): Int {
        return usageEventDao.getEventCountForDate(date)
    }
    
    // Helper methods for date formatting
    fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    fun getDateString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    fun getDateStringForDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    fun getDateRangeForLastDays(days: Int): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_YEAR, -days + 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        return Pair(startDate, endDate)
    }
}
