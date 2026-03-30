package com.mindguard.service

import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.UsageEvent
import com.mindguard.data.model.SessionType
import com.mindguard.data.repository.AppCategoryRepository
import com.mindguard.data.repository.UsageEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTracker @Inject constructor(
    private val appCategoryRepository: AppCategoryRepository,
    private val usageEventRepository: UsageEventRepository
) {
    private val mutex = Mutex()
    
    // In-memory tracking data
    private val currentSessions = mutableMapOf<String, AppSession>()
    private val dailyTotals = mutableMapOf<String, Int>()
    private var lastKnownApp: String? = null
    
    // StateFlow for observing current state
    private val _currentForegroundApp = MutableStateFlow<String?>(null)
    val currentForegroundApp: StateFlow<String?> = _currentForegroundApp
    
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking
    
    data class AppSession(
        val packageName: String,
        val appLabel: String,
        val category: AppCategory,
        var startTime: Long,
        var lastUpdateTime: Long,
        var continuousMinutes: Int,
        var isResumed: Boolean = false
    )
    
    suspend fun startTracking() {
        mutex.withLock {
            _isTracking.value = true
            resetDailyTotalsIfNeeded()
        }
    }
    
    suspend fun stopTracking() {
        mutex.withLock {
            _isTracking.value = false
            // End all active sessions
            currentSessions.values.forEach { session ->
                endSession(session)
            }
            currentSessions.clear()
        }
    }
    
    suspend fun updateForegroundApp(packageName: String?, appLabel: String? = null) {
        if (!_isTracking.value) return
        
        mutex.withLock {
            val previousApp = _currentForegroundApp.value
            _currentForegroundApp.value = packageName
            
            if (packageName == null || packageName == previousApp) {
                // No change or null (screen off/launcher)
                return@withLock
            }
            
            // End previous session if exists
            previousApp?.let { prevPkg ->
                currentSessions[prevPkg]?.let { session ->
                    session.lastUpdateTime = System.currentTimeMillis()
                    session.isResumed = true // Mark as potentially resumable
                }
            }
            
            // Start or resume new session
            val category = appCategoryRepository.getCategoryForPackage(packageName)
            val label = appLabel ?: getAppLabel(packageName)
            
            val existingSession = currentSessions[packageName]
            if (existingSession != null && existingSession.isResumed) {
                // Resume existing session
                existingSession.lastUpdateTime = System.currentTimeMillis()
                existingSession.isResumed = false
            } else {
                // Start new session
                val session = AppSession(
                    packageName = packageName,
                    appLabel = label,
                    category = category,
                    startTime = System.currentTimeMillis(),
                    lastUpdateTime = System.currentTimeMillis(),
                    continuousMinutes = 0
                )
                currentSessions[packageName] = session
            }
        }
    }
    
    suspend fun processTick() {
        if (!_isTracking.value) return
        
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val currentDate = getCurrentDateString()
            
            currentSessions.values.forEach { session ->
                val minutesSinceLastUpdate = ((currentTime - session.lastUpdateTime) / 60000).toInt()
                if (minutesSinceLastUpdate > 0) {
                    session.continuousMinutes += minutesSinceLastUpdate
                    session.lastUpdateTime = currentTime
                    
                    // Update daily total
                    dailyTotals[session.packageName] = (dailyTotals[session.packageName] ?: 0) + minutesSinceLastUpdate
                }
            }
            
            // Check for sessions that should be ended (no activity for 5 minutes)
            val sessionsToEnd = currentSessions.filter { (_, session) ->
                (currentTime - session.lastUpdateTime) > 5 * 60 * 1000
            }
            
            sessionsToEnd.forEach { (packageName, session) ->
                endSession(session)
                currentSessions.remove(packageName)
            }
        }
    }
    
    private suspend fun endSession(session: AppSession) {
        val endTime = System.currentTimeMillis()
        val durationMinutes = ((endTime - session.startTime) / 60000).toInt()
        
        if (durationMinutes > 0) {
            val sessionType = if (session.isResumed) SessionType.RESUMED else SessionType.CONTINUOUS
            val usageEvent = UsageEvent(
                packageName = session.packageName,
                appLabel = session.appLabel,
                category = session.category,
                startTimestamp = session.startTime,
                endTimestamp = endTime,
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                date = getCurrentDateString()
            )
            
            usageEventRepository.insertEvent(usageEvent)
        }
    }
    
    suspend fun forceFlushAllSessions() {
        mutex.withLock {
            currentSessions.values.forEach { session ->
                endSession(session)
            }
            currentSessions.clear()
        }
    }
    
    fun getCurrentSession(packageName: String): AppSession? {
        return currentSessions[packageName]
    }
    
    fun getAllCurrentSessions(): Map<String, AppSession> {
        return currentSessions.toMap()
    }
    
    fun getDailyTotal(packageName: String): Int {
        return dailyTotals[packageName] ?: 0
    }
    
    fun getAllDailyTotals(): Map<String, Int> {
        return dailyTotals.toMap()
    }
    
    fun getContinuousMinutes(packageName: String): Int {
        return currentSessions[packageName]?.continuousMinutes ?: 0
    }
    
    fun getCumulativeMinutes(packageName: String): Int {
        val continuous = currentSessions[packageName]?.continuousMinutes ?: 0
        val daily = dailyTotals[packageName] ?: 0
        return continuous + daily
    }
    
    private fun resetDailyTotalsIfNeeded() {
        val currentDate = getCurrentDateString()
        // This would be enhanced to track date changes and reset totals
        // For now, we'll assume this is called at the start of each day
    }
    
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun getAppLabel(packageName: String): String {
        // This would need context to get the actual app label
        // For now, return the package name
        return packageName
    }
    
    suspend fun getDailyStats(): DailyStats {
        return mutex.withLock {
            val totalScreenTime = dailyTotals.values.sum()
            val categoryStats = mutableMapOf<AppCategory, Int>()
            
            currentSessions.values.forEach { session ->
                categoryStats[session.category] = (categoryStats[session.category] ?: 0) + session.continuousMinutes
            }
            
            DailyStats(
                totalScreenTimeMinutes = totalScreenTime,
                categoryBreakdown = categoryStats,
                activeSessionCount = currentSessions.size,
                currentForegroundApp = _currentForegroundApp.value
            )
        }
    }
    
    data class DailyStats(
        val totalScreenTimeMinutes: Int,
        val categoryBreakdown: Map<AppCategory, Int>,
        val activeSessionCount: Int,
        val currentForegroundApp: String?
    )
}
