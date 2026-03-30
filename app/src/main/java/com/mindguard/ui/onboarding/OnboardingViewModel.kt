package com.mindguard.ui.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.StrictLevel
import com.mindguard.data.repository.AppCategoryRepository
import com.mindguard.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val appCategoryRepository: AppCategoryRepository
) : ViewModel() {
    
    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain
    
    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> = _currentPage
    
    private val _showPermissionDialog = MutableLiveData<String>()
    val showPermissionDialog: LiveData<String> = _showPermissionDialog
    
    // User profile data
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName
    
    private val _userGoal = MutableLiveData<String>()
    val userGoal: LiveData<String> = _userGoal
    
    private val _workHoursStart = MutableLiveData<String>()
    val workHoursStart: LiveData<String> = _workHoursStart
    
    private val _workHoursEnd = MutableLiveData<String>()
    val workHoursEnd: LiveData<String> = _workHoursEnd
    
    private val _strictLevel = MutableLiveData<StrictLevel>()
    val strictLevel: LiveData<StrictLevel> = _strictLevel
    
    // Permission tracking
    private val permissionStatus = mutableMapOf<String, Boolean>()
    
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_GOAL = stringPreferencesKey("user_goal")
        private val WORK_HOURS_START = stringPreferencesKey("work_hours_start")
        private val WORK_HOURS_END = stringPreferencesKey("work_hours_end")
        private val STRICT_LEVEL = stringPreferencesKey("strict_level")
    }
    
    init {
        viewModelScope.launch {
            checkOnboardingStatus()
        }
    }
    
    private suspend fun checkOnboardingStatus() {
        val preferences = dataStore.data.first()
        val completed = preferences[ONBOARDING_COMPLETED] ?: false
        
        if (completed) {
            _navigateToMain.value = true
        }
    }
    
    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }
    
    fun onPermissionResult(permission: String, granted: Boolean) {
        permissionStatus[permission] = granted
        
        if (!granted) {
            _showPermissionDialog.value = permission
        }
    }
    
    fun setUserName(name: String) {
        _userName.value = name
    }
    
    fun setUserGoal(goal: String) {
        _userGoal.value = goal
    }
    
    fun setWorkHours(start: String, end: String) {
        _workHoursStart.value = start
        _workHoursEnd.value = end
    }
    
    fun setStrictLevel(level: StrictLevel) {
        _strictLevel.value = level
    }
    
    fun areAllPermissionsGranted(): Boolean {
        val permissionManager = PermissionManager()
        // This would be checked in the activity context
        return true // Placeholder - actual check would need context
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED] = true
                preferences[USER_NAME] = _userName.value ?: ""
                preferences[USER_GOAL] = _userGoal.value ?: "General Wellness"
                preferences[WORK_HOURS_START] = _workHoursStart.value ?: "09:00"
                preferences[WORK_HOURS_END] = _workHoursEnd.value ?: "18:00"
                preferences[STRICT_LEVEL] = (_strictLevel.value ?: StrictLevel.BALANCED).name
            }
            
            _navigateToMain.value = true
        }
    }
    
    fun skipOnboarding() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED] = true
                preferences[USER_NAME] = "User"
                preferences[USER_GOAL] = "General Wellness"
                preferences[WORK_HOURS_START] = "09:00"
                preferences[WORK_HOURS_END] = "18:00"
                preferences[STRICT_LEVEL] = StrictLevel.BALANCED.name
            }
            
            _navigateToMain.value = true
        }
    }
}
