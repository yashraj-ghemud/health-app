package com.mindguard.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.data.repository.AchievementRepository
import com.mindguard.domain.engine.AchievementEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val achievementEngine: AchievementEngine
) : ViewModel() {
    
    private val _achievements = MutableStateFlow<List<com.mindguard.data.model.Achievement>>(emptyList())
    val achievements: StateFlow<List<com.mindguard.data.model.Achievement>> = _achievements.asStateFlow()
    
    private val _achievementProgress = MutableStateFlow<AchievementEngine.AchievementProgress?>(null)
    val achievementProgress: StateFlow<AchievementEngine.AchievementProgress?> = _achievementProgress.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        refreshAchievements()
    }
    
    fun refreshAchievements() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Load all achievements
                achievementRepository.getAllAchievements().collect { allAchievements ->
                    _achievements.value = allAchievements
                    
                    // Mark new achievements as viewed
                    val newAchievements = allAchievements.filter { it.isNew }
                    if (newAchievements.isNotEmpty()) {
                        achievementRepository.markAllAsViewed()
                    }
                }
                
                // Load achievement progress
                val progress = achievementEngine.getAchievementProgress()
                _achievementProgress.value = progress
                
                // Check for any new milestone achievements
                achievementEngine.checkMilestoneAchievements()
                
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing achievements")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getAchievementsByCategory(category: com.mindguard.data.model.AchievementCategory) {
        viewModelScope.launch {
            try {
                achievementRepository.getAllAchievements().collect { allAchievements ->
                    val filteredAchievements = allAchievements.filter { it.category == category }
                    _achievements.value = filteredAchievements
                }
            } catch (e: Exception) {
                Timber.e(e, "Error filtering achievements by category")
            }
        }
    }
    
    fun getUnlockedAchievements() {
        viewModelScope.launch {
            try {
                achievementRepository.getUnlockedAchievements().collect { unlockedAchievements ->
                    _achievements.value = unlockedAchievements
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading unlocked achievements")
            }
        }
    }
    
    fun getLockedAchievements() {
        viewModelScope.launch {
            try {
                val lockedAchievements = achievementRepository.getLockedAchievements()
                _achievements.value = lockedAchievements
            } catch (e: Exception) {
                Timber.e(e, "Error loading locked achievements")
            }
        }
    }
    
    fun getNewAchievements() {
        viewModelScope.launch {
            try {
                val newAchievements = achievementRepository.getNewAchievements()
                _achievements.value = newAchievements
                
                // Mark them as viewed
                newAchievements.forEach { achievement ->
                    achievementRepository.markAsViewed(achievement.achievementKey)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading new achievements")
            }
        }
    }
    
    fun markAchievementAsViewed(achievementKey: String) {
        viewModelScope.launch {
            try {
                achievementRepository.markAsViewed(achievementKey)
                
                // Refresh the list to update UI
                refreshAchievements()
            } catch (e: Exception) {
                Timber.e(e, "Error marking achievement as viewed")
            }
        }
    }
}
