package com.mindguard.domain.engine

import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.FocusSession
import com.mindguard.data.repository.FocusSessionRepository
import com.mindguard.service.BlocklistManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionManager @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository,
    private val blocklistManager: BlocklistManager
) {
    
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()
    
    private val _currentSession = MutableStateFlow<FocusSession?>(null)
    val currentSession: StateFlow<FocusSession?> = _currentSession.asStateFlow()
    
    private var activeSessionId: Long? = null
    
    suspend fun startFocusSession(
        goal: String,
        durationMinutes: Int,
        allowedCategories: List<AppCategory>,
        blockedPackages: List<String>
    ): Long? {
        return try {
            // Check if there's already an active session
            if (_isSessionActive.value) {
                Timber.w("Focus session already active")
                return null
            }
            
            // Create new focus session
            val session = FocusSession(
                startTime = System.currentTimeMillis(),
                endTime = null,
                durationMinutes = durationMinutes,
                goalLabel = goal,
                completed = false,
                allowedCategories = allowedCategories,
                blockedPackages = blockedPackages
            )
            
            val sessionId = focusSessionRepository.insertSession(session)
            
            // Block distracting apps
            blockedPackages.forEach { packageName ->
                managerScope.launch {
                    blocklistManager.addToBlocklist(
                        packageName = packageName,
                        durationMinutes = durationMinutes,
                        reason = "Focus session: $goal",
                        ruleId = "focus_session"
                    )
                }
            }
            
            activeSessionId = sessionId
            _isSessionActive.value = true
            _currentSession.value = session.copy(id = sessionId)
            
            Timber.d("Focus session started: $sessionId")
            sessionId
            
        } catch (e: Exception) {
            Timber.e(e, "Error starting focus session")
            null
        }
    }
    
    suspend fun stopFocusSession(): Boolean {
        return try {
            val sessionId = activeSessionId ?: return false
            val session = _currentSession.value ?: return false
            
            val endTime = System.currentTimeMillis()
            val actualDuration = ((endTime - session.startTime) / 60000).toInt()
            
            // Update session in database
            focusSessionRepository.completeSession(sessionId, endTime, actualDuration)
            
            // Clear all focus session blocks
            clearFocusSessionBlocks()
            
            // Reset state
            activeSessionId = null
            _isSessionActive.value = false
            _currentSession.value = null
            
            Timber.d("Focus session stopped: $sessionId")
            true
            
        } catch (e: Exception) {
            Timber.e(e, "Error stopping focus session")
            false
        }
    }
    
    suspend fun pauseFocusSession(): Boolean {
        return try {
            // For now, we don't actually pause - just update state
            // In a more complex implementation, we might pause the timer
            Timber.d("Focus session paused")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error pausing focus session")
            false
        }
    }
    
    suspend fun resumeFocusSession(): Boolean {
        return try {
            Timber.d("Focus session resumed")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error resuming focus session")
            false
        }
    }
    
    suspend fun extendSession(additionalMinutes: Int): Boolean {
        return try {
            val sessionId = activeSessionId ?: return false
            val currentSession = _currentSession.value ?: return false
            
            // Extend the session duration
            val newDuration = currentSession.durationMinutes + additionalMinutes
            
            // Update session
            val updatedSession = currentSession.copy(durationMinutes = newDuration)
            focusSessionRepository.updateSession(updatedSession)
            _currentSession.value = updatedSession
            
            // Extend blocks for blocked packages
            currentSession.blockedPackages.forEach { packageName ->
                managerScope.launch {
                    blocklistManager.extendBlock(packageName, additionalMinutes)
                }
            }
            
            Timber.d("Focus session extended by $additionalMinutes minutes")
            true
            
        } catch (e: Exception) {
            Timber.e(e, "Error extending focus session")
            false
        }
    }
    
    suspend fun addAllowedCategory(category: AppCategory): Boolean {
        return try {
            val currentSession = _currentSession.value ?: return false
            
            if (!currentSession.allowedCategories.contains(category)) {
                val updatedCategories = currentSession.allowedCategories + category
                val updatedSession = currentSession.copy(allowedCategories = updatedCategories)
                
                focusSessionRepository.updateSession(updatedSession)
                _currentSession.value = updatedSession
                
                Timber.d("Added allowed category: $category")
            }
            
            true
            
        } catch (e: Exception) {
            Timber.e(e, "Error adding allowed category")
            false
        }
    }
    
    suspend fun removeAllowedCategory(category: AppCategory): Boolean {
        return try {
            val currentSession = _currentSession.value ?: return false
            
            if (currentSession.allowedCategories.contains(category)) {
                val updatedCategories = currentSession.allowedCategories - category
                val updatedSession = currentSession.copy(allowedCategories = updatedCategories)
                
                focusSessionRepository.updateSession(updatedSession)
                _currentSession.value = updatedSession
                
                // Block apps from this category
                // This would require querying the app category repository
                
                Timber.d("Removed allowed category: $category")
            }
            
            true
            
        } catch (e: Exception) {
            Timber.e(e, "Error removing allowed category")
            false
        }
    }
    
    suspend fun isPackageBlocked(packageName: String): Boolean {
        if (!_isSessionActive.value) return false
        
        val currentSession = _currentSession.value ?: return false
        return currentSession.blockedPackages.contains(packageName)
    }
    
    suspend fun isCategoryAllowed(category: AppCategory): Boolean {
        if (!_isSessionActive.value) return true
        
        val currentSession = _currentSession.value ?: return false
        return currentSession.allowedCategories.contains(category)
    }
    
    private suspend fun clearFocusSessionBlocks() {
        try {
            val currentSession = _currentSession.value ?: return
            
            // Remove blocks for all packages blocked by this focus session
            currentSession.blockedPackages.forEach { packageName ->
                managerScope.launch {
                    blocklistManager.removeFromBlocklist(packageName)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error clearing focus session blocks")
        }
    }
    
    suspend fun getSessionStatistics(): FocusSessionStatistics? {
        return try {
            val currentSession = _currentSession.value ?: return null
            
            val elapsedMinutes = if (currentSession.endTime != null) {
                currentSession.durationMinutes
            } else {
                ((System.currentTimeMillis() - currentSession.startTime) / 60000).toInt()
            }
            
            FocusSessionStatistics(
                sessionId = currentSession.id,
                goalLabel = currentSession.goalLabel,
                startTime = currentSession.startTime,
                elapsedMinutes = elapsedMinutes,
                targetMinutes = currentSession.durationMinutes,
                isCompleted = currentSession.completed,
                allowedCategories = currentSession.allowedCategories,
                blockedPackagesCount = currentSession.blockedPackages.size
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error getting session statistics")
            null
        }
    }
    
    data class FocusSessionStatistics(
        val sessionId: Long?,
        val goalLabel: String,
        val startTime: Long,
        val elapsedMinutes: Int,
        val targetMinutes: Int,
        val isCompleted: Boolean,
        val allowedCategories: List<AppCategory>,
        val blockedPackagesCount: Int
    )
}
