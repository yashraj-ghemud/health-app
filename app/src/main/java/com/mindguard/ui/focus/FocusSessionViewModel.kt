package com.mindguard.ui.focus

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.data.model.AppCategory
import com.mindguard.data.repository.FocusSessionRepository
import com.mindguard.domain.engine.FocusSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository,
    private val focusSessionManager: FocusSessionManager
) : ViewModel() {
    
    private val _sessionState = MutableLiveData<FocusSessionState>()
    val sessionState: LiveData<FocusSessionState> = _sessionState
    
    private val _remainingTime = MutableLiveData<Int>()
    val remainingTime: LiveData<Int> = _remainingTime
    
    private val _sessionProgress = MutableLiveData<Int>()
    val sessionProgress: LiveData<Int> = _sessionProgress
    
    private val _currentSession = MutableLiveData<com.mindguard.data.model.FocusSession?>()
    val currentSession: LiveData<com.mindguard.data.model.FocusSession?> = _currentSession
    
    private val _sessionDuration = MutableLiveData<Int>()
    val sessionDuration: LiveData<Int> = _sessionDuration
    
    private var sessionStartTime: Long = 0
    private var totalSessionMinutes: Int = 25
    
    init {
        checkActiveSession()
        _sessionState.value = FocusSessionState.SETUP
        _sessionDuration.value = 25
    }
    
    private fun checkActiveSession() {
        viewModelScope.launch {
            try {
                val activeSession = focusSessionRepository.getActiveSession()
                if (activeSession != null) {
                    _currentSession.value = activeSession
                    _sessionState.value = FocusSessionState.ACTIVE
                    sessionStartTime = activeSession.startTime
                    totalSessionMinutes = activeSession.durationMinutes
                    
                    // Calculate remaining time
                    val elapsedMinutes = ((System.currentTimeMillis() - sessionStartTime) / 60000).toInt()
                    val remaining = maxOf(0, totalSessionMinutes - elapsedMinutes)
                    _remainingTime.value = remaining
                    
                    // Update progress
                    val progress = ((elapsedMinutes.toFloat() / totalSessionMinutes) * 100).toInt()
                    _sessionProgress.value = progress.coerceIn(0, 100)
                    
                    // Start timer if session is still active
                    if (remaining > 0) {
                        startSessionTimer(remaining)
                    } else {
                        completeSession()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking active session")
            }
        }
    }
    
    fun setSessionDuration(minutes: Int) {
        totalSessionMinutes = minutes
        _sessionDuration.value = minutes
    }
    
    fun startFocusSession(goal: String, allowedCategories: Set<AppCategory>, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val blockedPackages = getBlockedPackages(allowedCategories)
                
                val sessionId = focusSessionManager.startFocusSession(
                    goal = goal,
                    durationMinutes = totalSessionMinutes,
                    allowedCategories = allowedCategories.toList(),
                    blockedPackages = blockedPackages
                )
                
                if (sessionId != null) {
                    sessionStartTime = System.currentTimeMillis()
                    _sessionState.value = FocusSessionState.ACTIVE
                    
                    // Load the created session
                    val session = focusSessionRepository.getCompletedSessions().first().firstOrNull { it.id == sessionId }
                    _currentSession.value = session
                    
                    // Start timer
                    startSessionTimer(totalSessionMinutes)
                    
                    callback(true)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error starting focus session")
                callback(false)
            }
        }
    }
    
    fun pauseFocusSession() {
        _sessionState.value = FocusSessionState.PAUSED
        // Timer continues but we pause the breathing animation
    }
    
    fun resumeFocusSession() {
        _sessionState.value = FocusSessionState.ACTIVE
    }
    
    fun stopFocusSession(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = focusSessionManager.stopFocusSession()
                if (success) {
                    completeSession()
                    callback(true)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error stopping focus session")
                callback(false)
            }
        }
    }
    
    private fun startSessionTimer(remainingMinutes: Int) {
        val totalMillis = remainingMinutes * 60 * 1000L
        
        object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutesRemaining = (millisUntilFinished / 60000).toInt()
                _remainingTime.value = minutesRemaining
                
                // Update progress
                val elapsedMinutes = totalSessionMinutes - minutesRemaining
                val progress = ((elapsedMinutes.toFloat() / totalSessionMinutes) * 100).toInt()
                _sessionProgress.value = progress.coerceIn(0, 100)
            }
            
            override fun onFinish() {
                _remainingTime.value = 0
                _sessionProgress.value = 100
                completeSession()
            }
        }.start()
    }
    
    private fun completeSession() {
        _sessionState.value = FocusSessionState.COMPLETED
        _remainingTime.value = 0
        _sessionProgress.value = 100
    }
    
    private suspend fun getBlockedPackages(allowedCategories: Set<AppCategory>): List<String> {
        // Get all packages that should be blocked during focus session
        val blockedCategories = AppCategory.values().toSet() - allowedCategories
        
        // This would typically query the app category repository
        // For now, return common distracting apps
        return listOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.tiktok",
            "com.snapchat.android",
            "com.reddit.frontpage",
            "com.netflix.mediaclient",
            "com.youtube.android"
        )
    }
    
    fun getCompletedSessionDuration(): Int {
        return totalSessionMinutes
    }
    
    fun getCurrentSessionGoal(): String {
        return _currentSession.value?.goalLabel ?: "Focus Session"
    }
    
    fun getSessionStats(): FocusSessionStats {
        val current = _currentSession.value
        return if (current != null) {
            val elapsedMinutes = ((System.currentTimeMillis() - sessionStartTime) / 60000).toInt()
            FocusSessionStats(
                durationMinutes = elapsedMinutes,
                goalMinutes = totalSessionMinutes,
                goalLabel = current.goalLabel,
                isCompleted = _sessionState.value == FocusSessionState.COMPLETED
            )
        } else {
            FocusSessionStats(
                durationMinutes = 0,
                goalMinutes = totalSessionMinutes,
                goalLabel = "",
                isCompleted = false
            )
        }
    }
}

enum class FocusSessionState {
    SETUP,
    ACTIVE,
    PAUSED,
    COMPLETED
}

data class FocusSessionStats(
    val durationMinutes: Int,
    val goalMinutes: Int,
    val goalLabel: String,
    val isCompleted: Boolean
)
