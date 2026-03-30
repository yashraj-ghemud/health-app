package com.mindguard.ui.focus

import androidx.lifecycle.ViewModel
import com.mindguard.data.repository.FocusSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SessionCompleteViewModel @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository
) : ViewModel() {
    
    private val _sessionStats = MutableStateFlow<SessionStats?>(null)
    val sessionStats: StateFlow<SessionStats?> = _sessionStats.asStateFlow()
    
    data class SessionStats(
        val durationMinutes: Int,
        val goalLabel: String,
        val completedAt: Long,
        val isPersonalBest: Boolean
    )
    
    fun loadSessionStats(sessionId: Long) {
        // This would load the completed session stats from repository
        // For now, we'll use the data passed from the intent
    }
}
