package com.mindguard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.data.repository.UsageEventRepository
import com.mindguard.service.UsageMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usageEventRepository: UsageEventRepository
) : ViewModel() {
    
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning
    
    private val _todayStats = MutableStateFlow<TodayStats?>(null)
    val todayStats: StateFlow<TodayStats?> = _todayStats
    
    data class TodayStats(
        val totalScreenTime: Int,
        val productiveTime: Int,
        val entertainmentTime: Int,
        val communicationTime: Int,
        val wellnessScore: Int
    )
    
    init {
        checkServiceStatus()
        loadTodayStats()
    }
    
    private fun checkServiceStatus() {
        // This would check if the UsageMonitorService is running
        // For now, we'll assume it should be running
        _isServiceRunning.value = true
    }
    
    private fun loadTodayStats() {
        viewModelScope.launch {
            try {
                val today = usageEventRepository.getCurrentDateString()
                val totalTime = usageEventRepository.getTotalScreenTime(today)
                val productiveTime = usageEventRepository.getProductiveTime(today)
                val entertainmentTime = usageEventRepository.getEntertainmentTime(today)
                val communicationTime = usageEventRepository.getCommunicationTime(today)
                
                val wellnessScore = calculateWellnessScore(
                    totalTime = totalTime,
                    productiveTime = productiveTime,
                    entertainmentTime = entertainmentTime,
                    communicationTime = communicationTime
                )
                
                _todayStats.value = TodayStats(
                    totalScreenTime = totalTime,
                    productiveTime = productiveTime,
                    entertainmentTime = entertainmentTime,
                    communicationTime = communicationTime,
                    wellnessScore = wellnessScore
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading today's stats")
            }
        }
    }
    
    private fun calculateWellnessScore(
        totalTime: Int,
        productiveTime: Int,
        entertainmentTime: Int,
        communicationTime: Int
    ): Int {
        // Simple wellness score calculation
        var score = 100
        
        // Deduct points for excessive entertainment time
        if (entertainmentTime > 180) { // More than 3 hours
            score -= 20
        } else if (entertainmentTime > 120) { // More than 2 hours
            score -= 10
        }
        
        // Add points for productive time
        if (productiveTime > 240) { // More than 4 hours
            score += 10
        } else if (productiveTime > 120) { // More than 2 hours
            score += 5
        }
        
        // Deduct points for excessive total screen time
        if (totalTime > 480) { // More than 8 hours
            score -= 15
        } else if (totalTime > 360) { // More than 6 hours
            score -= 10
        }
        
        return score.coerceIn(0, 100)
    }
    
    fun refreshStats() {
        loadTodayStats()
    }
}
