package com.mindguard.data.repository

import com.mindguard.data.db.FocusSessionDao
import com.mindguard.data.model.FocusSession
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) {
    
    fun getAllSessions(): Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    
    fun getCompletedSessions(): Flow<List<FocusSession>> = focusSessionDao.getCompletedSessions()
    
    suspend fun getActiveSession(): FocusSession? {
        return focusSessionDao.getActiveSession()
    }
    
    suspend fun getSessionsForDate(date: String): List<FocusSession> {
        return focusSessionDao.getSessionsForDate(date)
    }
    
    suspend fun getSessionsForDateRange(startDate: Long, endDate: Long): List<FocusSession> {
        return focusSessionDao.getSessionsForDateRange(startDate, endDate)
    }
    
    suspend fun getCompletedSessionsCount(date: String): Int {
        return focusSessionDao.getCompletedSessionsCount(date)
    }
    
    suspend fun getTotalFocusMinutes(date: String): Int {
        return focusSessionDao.getTotalFocusMinutes(date) ?: 0
    }
    
    suspend fun getAverageSessionDuration(): Double {
        return focusSessionDao.getAverageSessionDuration() ?: 0.0
    }
    
    suspend fun insertSession(session: FocusSession): Long {
        return focusSessionDao.insertSession(session)
    }
    
    suspend fun updateSession(session: FocusSession) {
        focusSessionDao.updateSession(session)
    }
    
    suspend fun completeSession(id: Long, endTime: Long, duration: Int) {
        focusSessionDao.completeSession(id, endTime, duration, true)
    }
    
    suspend fun cancelSession(id: Long, endTime: Long, duration: Int) {
        focusSessionDao.completeSession(id, endTime, duration, false)
    }
    
    suspend fun deleteSession(session: FocusSession) {
        focusSessionDao.deleteSession(session)
    }
    
    suspend fun deleteSessionById(id: Long) {
        focusSessionDao.deleteSessionById(id)
    }
    
    suspend fun deleteOldSessions(cutoffTimestamp: Long) {
        focusSessionDao.deleteOldSessions(cutoffTimestamp)
    }
    
    // Helper methods
    fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    fun getDateString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    suspend fun hasActiveSession(): Boolean {
        return getActiveSession() != null
    }
    
    suspend fun getTodayStats(): Pair<Int, Int> {
        val today = getCurrentDateString()
        val count = getCompletedSessionsCount(today)
        val minutes = getTotalFocusMinutes(today)
        return Pair(count, minutes)
    }
    
    suspend fun getWeeklyStats(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = calendar.timeInMillis
        
        val sessions = getSessionsForDateRange(startDate, endDate)
        val completedSessions = sessions.filter { it.completed }
        val totalMinutes = completedSessions.sumOf { it.durationMinutes }
        
        return Pair(completedSessions.size, totalMinutes)
    }
}
