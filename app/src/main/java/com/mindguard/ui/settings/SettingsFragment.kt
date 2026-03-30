package com.mindguard.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.mindguard.R
import com.mindguard.ui.settings.categories.AppCategoriesActivity
import com.mindguard.ui.settings.rules.RulesActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        setupPreferences()
        observeViewModel()
    }
    
    private fun setupPreferences() {
        // App Categories
        findPreference<Preference>("app_categories")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), AppCategoriesActivity::class.java))
            true
        }
        
        // Rules Management
        findPreference<Preference>("rules_management")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), RulesActivity::class.java))
            true
        }
        
        // Focus Mode Settings
        findPreference<Preference>("focus_settings")?.setOnPreferenceClickListener {
            showFocusSettingsDialog()
            true
        }
        
        // Notifications
        findPreference<SwitchPreferenceCompat>("notifications_enabled")?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setNotificationsEnabled(newValue as Boolean)
            true
        }
        
        // Dark Mode
        findPreference<SwitchPreferenceCompat>("dark_mode")?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setDarkModeEnabled(newValue as Boolean)
            true
        }
        
        // Export Data
        findPreference<Preference>("export_data")?.setOnPreferenceClickListener {
            exportUserData()
            true
        }
        
        // Reset Settings
        findPreference<Preference>("reset_settings")?.setOnPreferenceClickListener {
            showResetSettingsDialog()
            true
        }
        
        // About
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            showAboutDialog()
            true
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.settingsState.collect { state ->
                    updatePreferenceStates(state)
                }
            }
        }
    }
    
    private fun updatePreferenceStates(state: SettingsState) {
        (findPreference<Preference>("notifications_enabled") as? SwitchPreferenceCompat)?.isChecked = state.notificationsEnabled
        (findPreference<Preference>("dark_mode") as? SwitchPreferenceCompat)?.isChecked = state.darkModeEnabled
        findPreference<Preference>("focus_duration")?.summary = "${state.focusSessionDuration} minutes"
        findPreference<Preference>("sleep_hours")?.summary = "${state.sleepStart} - ${state.sleepEnd}"
    }
    
    private fun showFocusSettingsDialog() {
        // Create focus duration options
        val durations = arrayOf("15 minutes", "25 minutes", "45 minutes", "60 minutes", "90 minutes", "Custom")
        val currentDuration = viewModel.settingsState.value.focusSessionDuration
        val currentIndex = when (currentDuration) {
            15 -> 0
            25 -> 1
            45 -> 2
            60 -> 3
            90 -> 4
            else -> 1 // Default to 25 minutes
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Focus Session Duration")
            .setSingleChoiceItems(durations, currentIndex) { dialog, which ->
                val selectedDuration = when (which) {
                    0 -> 15
                    1 -> 25
                    2 -> 45
                    3 -> 60
                    4 -> 90
                    5 -> {
                        // Show custom duration dialog
                        dialog.dismiss()
                        showCustomDurationDialog()
                        return@setSingleChoiceItems
                    }
                    else -> 25
                }
                viewModel.setFocusSessionDuration(selectedDuration)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCustomDurationDialog() {
        // Create a simple input dialog for custom duration
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Custom Focus Duration")
            .setMessage("Enter focus session duration in minutes (5-180):")
            .setView(android.widget.EditText(requireContext()).apply {
                id = android.R.id.edit
                hint = "25"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText("25")
            })
            .setPositiveButton("Set") { dialog, _ ->
                val editText = (dialog as? androidx.appcompat.app.AlertDialog)?.findViewById<android.widget.EditText>(android.R.id.edit)
                val customDuration = editText?.text.toString().toIntOrNull()
                if (customDuration != null && customDuration in 5..180) {
                    viewModel.setFocusSessionDuration(customDuration)
                } else {
                    // Show error dialog
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Invalid Duration")
                        .setMessage("Please enter a duration between 5 and 180 minutes.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val exportPath = viewModel.exportUserData()
                showExportSuccessDialog(exportPath)
            } catch (e: Exception) {
                Timber.e(e, "Error exporting user data")
                showExportErrorDialog()
            }
        }
    }
    
    private fun showExportSuccessDialog(exportPath: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Export Successful")
            .setMessage("Your data has been exported to:\n$exportPath")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showExportErrorDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Export Failed")
            .setMessage("Failed to export your data. Please try again.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showResetSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("This will reset all your settings to default values. Your data will not be deleted.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetSettings()
                showResetSuccessDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showResetSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Settings Reset")
            .setMessage("All settings have been reset to default values.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showAboutDialog() {
        val aboutText = """
            MindGuard v1.0
            
            Your digital wellness companion for building healthier phone habits.
            
            Features:
            • Smart app usage monitoring
            • Customizable intervention rules
            • Focus sessions with breathing exercises
            • Daily wellness scoring
            • Achievement system
            • Beautiful analytics dashboard
            
            Created with ❤️ for better digital wellbeing.
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("About MindGuard")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show()
    }
}
