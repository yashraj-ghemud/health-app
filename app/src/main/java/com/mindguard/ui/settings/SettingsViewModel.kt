package com.mindguard.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.data.repository.AppCategoryRepository
import com.mindguard.data.repository.UsageRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val appCategoryRepository: AppCategoryRepository,
    private val usageRuleRepository: UsageRuleRepository
) : ViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val FOCUS_SESSION_DURATION = intPreferencesKey("focus_session_duration")
        private val SLEEP_START = stringPreferencesKey("sleep_start")
        private val SLEEP_END = stringPreferencesKey("sleep_end")
        private val STRICT_LEVEL = stringPreferencesKey("strict_level")
        private val AUTO_START_SERVICE = booleanPreferencesKey("auto_start_service")
    }
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val preferences = dataStore.data.first()
                
                _settingsState.value = SettingsState(
                    notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                    darkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
                    focusSessionDuration = preferences[FOCUS_SESSION_DURATION] ?: 25,
                    sleepStart = preferences[SLEEP_START] ?: "23:00",
                    sleepEnd = preferences[SLEEP_END] ?: "06:00",
                    strictLevel = preferences[STRICT_LEVEL] ?: "BALANCED",
                    autoStartService = preferences[AUTO_START_SERVICE] ?: true
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading settings")
            }
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[NOTIFICATIONS_ENABLED] = enabled
                }
                _settingsState.value = _settingsState.value.copy(notificationsEnabled = enabled)
            } catch (e: Exception) {
                Timber.e(e, "Error setting notifications enabled")
            }
        }
    }
    
    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[DARK_MODE_ENABLED] = enabled
                }
                _settingsState.value = _settingsState.value.copy(darkModeEnabled = enabled)
            } catch (e: Exception) {
                Timber.e(e, "Error setting dark mode enabled")
            }
        }
    }
    
    fun setFocusSessionDuration(durationMinutes: Int) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[FOCUS_SESSION_DURATION] = durationMinutes
                }
                _settingsState.value = _settingsState.value.copy(focusSessionDuration = durationMinutes)
            } catch (e: Exception) {
                Timber.e(e, "Error setting focus session duration")
            }
        }
    }
    
    fun setSleepHours(start: String, end: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[SLEEP_START] = start
                    preferences[SLEEP_END] = end
                }
                _settingsState.value = _settingsState.value.copy(sleepStart = start, sleepEnd = end)
            } catch (e: Exception) {
                Timber.e(e, "Error setting sleep hours")
            }
        }
    }
    
    fun setStrictLevel(level: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[STRICT_LEVEL] = level
                }
                _settingsState.value = _settingsState.value.copy(strictLevel = level)
            } catch (e: Exception) {
                Timber.e(e, "Error setting strict level")
            }
        }
    }
    
    fun setAutoStartService(enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[AUTO_START_SERVICE] = enabled
                }
                _settingsState.value = _settingsState.value.copy(autoStartService = enabled)
            } catch (e: Exception) {
                Timber.e(e, "Error setting auto start service")
            }
        }
    }
    
    suspend fun exportUserData(): String {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "mindguard_export_$timestamp.csv"
            val file = File(getExportDirectory(), fileName)
            
            FileWriter(file).use { writer ->
                // Write header
                writer.appendLine("Date,Wellness Score,Total Screen Time,Productive Time,Entertainment Time,Communication Time,Interventions,Focus Sessions")
                
                // Write data (this would query the actual data from repositories)
                writer.appendLine("2024-01-01,75,240,120,60,45,3,2")
                writer.appendLine("2024-01-02,82,220,140,40,30,2,3")
                writer.appendLine("2024-01-03,68,260,100,80,40,5,1")
                // ... more data
            }
            
            file.absolutePath
            
        } catch (e: Exception) {
            Timber.e(e, "Error exporting user data")
            throw e
        }
    }
    
    private fun getExportDirectory(): File {
        val downloadsDir = File(android.os.Environment.getExternalStorageDirectory(), "Downloads")
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        return downloadsDir
    }
    
    fun resetSettings() {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences.clear()
                }
                
                // Reset to defaults
                _settingsState.value = SettingsState()
                
                // Reset app categories to defaults
                appCategoryRepository.resetToDefaults()
                
                // Reset rules to defaults
                // This would reload the default rules
                
                Timber.d("Settings reset to defaults")
                
            } catch (e: Exception) {
                Timber.e(e, "Error resetting settings")
            }
        }
    }
    
    suspend fun exportAppCategories(): String {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "mindguard_app_categories_$timestamp.csv"
            val file = File(getExportDirectory(), fileName)
            
            FileWriter(file).use { writer ->
                writer.appendLine("Package Name,Category,Is User Defined")
                
                val mappings = appCategoryRepository.getAllMappings().first()
                mappings.forEach { mapping ->
                    val isUserDefined = if (mapping.isUserDefined) "Yes" else "No"
                    writer.appendLine("${mapping.packageName},${mapping.category.name},$isUserDefined")
                }
            }
            
            file.absolutePath
            
        } catch (e: Exception) {
            Timber.e(e, "Error exporting app categories")
            throw e
        }
    }
    
    suspend fun importAppCategories(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            
            // Clear user-defined categories first
            appCategoryRepository.resetToDefaults()
            
            // Import from CSV file
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 2) {
                    val packageName = parts[0].trim()
                    val categoryName = parts[1].trim()
                    
                    try {
                        val category = com.mindguard.data.model.AppCategory.valueOf(categoryName)
                        appCategoryRepository.addCustomMapping(packageName, category)
                    } catch (e: IllegalArgumentException) {
                        Timber.w("Unknown category: $categoryName")
                    }
                }
            }
            
            true
            
        } catch (e: Exception) {
            Timber.e(e, "Error importing app categories")
            false
        }
    }
}

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val focusSessionDuration: Int = 25,
    val sleepStart: String = "23:00",
    val sleepEnd: String = "06:00",
    val strictLevel: String = "BALANCED",
    val autoStartService: Boolean = true
)
