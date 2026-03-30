package com.mindguard.data.db

import androidx.room.*
import com.mindguard.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageEventDao {
    
    @Query("SELECT * FROM usage_events WHERE date = :date ORDER BY startTimestamp DESC")
    fun getEventsForDate(date: String): Flow<List<UsageEvent>>
    
    @Query("SELECT * FROM usage_events WHERE date = :date ORDER BY startTimestamp DESC")
    suspend fun getEventsForDateSync(date: String): List<UsageEvent>
    
    @Query("SELECT * FROM usage_events WHERE date >= :startDate AND date <= :endDate ORDER BY startTimestamp DESC")
    fun getEventsForDateRange(startDate: String, endDate: String): Flow<List<UsageEvent>>
    
    @Query("SELECT * FROM usage_events WHERE packageName = :packageName AND date = :date")
    suspend fun getEventsForApp(packageName: String, date: String): List<UsageEvent>
    
    @Query("""
        SELECT category, SUM(durationMinutes) as totalMinutes 
        FROM usage_events 
        WHERE date = :date 
        GROUP BY category
    """)
    suspend fun getTimeByCategory(date: String): List<CategoryTimeSummary>
    
    @Query("""
        SELECT packageName, appLabel, category, SUM(durationMinutes) as totalMinutes 
        FROM usage_events 
        WHERE date = :date 
        GROUP BY packageName, appLabel, category 
        ORDER BY totalMinutes DESC 
        LIMIT :limit
    """)
    suspend fun getTopAppsForDate(date: String, limit: Int = 5): List<AppTimeSummary>
    
    @Query("SELECT SUM(durationMinutes) FROM usage_events WHERE date = :date")
    suspend fun getTotalScreenTime(date: String): Int?
    
    @Query("SELECT SUM(durationMinutes) FROM usage_events WHERE date = :date AND category IN ('DEEP_WORK', 'COMMUNICATION')")
    suspend fun getproductiveTime(date: String): Int?
    
    @Query("SELECT SUM(durationMinutes) FROM usage_events WHERE date = :date AND category = 'PASSIVE_ENTERTAINMENT'")
    suspend fun getEntertainmentTime(date: String): Int?
    
    @Query("SELECT SUM(durationMinutes) FROM usage_events WHERE date = :date AND category = 'COMMUNICATION'")
    suspend fun getCommunicationTime(date: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UsageEvent)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<UsageEvent>)
    
    @Query("DELETE FROM usage_events WHERE date < :cutoffDate")
    suspend fun deleteOldEvents(cutoffDate: String)
    
    @Query("SELECT COUNT(*) FROM usage_events WHERE date = :date")
    suspend fun getEventCountForDate(date: String): Int
}

data class CategoryTimeSummary(
    val category: AppCategory,
    val totalMinutes: Int
)

data class AppTimeSummary(
    val packageName: String,
    val appLabel: String,
    val category: AppCategory,
    val totalMinutes: Int
)
