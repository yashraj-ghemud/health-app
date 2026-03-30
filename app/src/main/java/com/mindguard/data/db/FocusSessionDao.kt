package com.mindguard.data.db

import androidx.room.*
import com.mindguard.data.model.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface FocusSessionDao {
    
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>
    
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    suspend fun getAllSessionsSync(): List<FocusSession>
    
    @Query("SELECT * FROM focus_sessions WHERE completed = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<FocusSession>>
    
    @Query("SELECT * FROM focus_sessions WHERE completed = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): FocusSession?
    
    @Query("SELECT * FROM focus_sessions WHERE date(startTime/1000, 'unixepoch') = :date ORDER BY startTime DESC")
    suspend fun getSessionsForDate(date: String): List<FocusSession>
    
    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    suspend fun getSessionsForDateRange(startDate: Long, endDate: Long): List<FocusSession>
    
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1 AND date(startTime/1000, 'unixepoch') = :date")
    suspend fun getCompletedSessionsCount(date: String): Int
    
    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE completed = 1 AND date(startTime/1000, 'unixepoch') = :date")
    suspend fun getTotalFocusMinutes(date: String): Int?
    
    @Query("SELECT AVG(durationMinutes) FROM focus_sessions WHERE completed = 1")
    suspend fun getAverageSessionDuration(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long
    
    @Update
    suspend fun updateSession(session: FocusSession)
    
    @Query("UPDATE focus_sessions SET endTime = :endTime, durationMinutes = :duration, completed = :completed WHERE id = :id")
    suspend fun completeSession(id: Long, endTime: Long, duration: Int, completed: Boolean)
    
    @Delete
    suspend fun deleteSession(session: FocusSession)
    
    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
    
    @Query("DELETE FROM focus_sessions WHERE startTime < :cutoffTimestamp")
    suspend fun deleteOldSessions(cutoffTimestamp: Long)
}
