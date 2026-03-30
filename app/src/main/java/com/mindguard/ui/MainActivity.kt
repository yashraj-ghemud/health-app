package com.mindguard.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.mindguard.databinding.ActivityMainBinding
import com.mindguard.ui.onboarding.OnboardingActivity
import com.mindguard.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var dataStore: DataStore<Preferences>
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        checkOnboardingStatus(savedInstanceState)
        setupUI(savedInstanceState)
        observeViewModel()
    }
    
    private fun checkOnboardingStatus(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            val preferences = dataStore.data.first()
            val onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false
            
            if (!onboardingCompleted) {
                navigateToOnboarding()
                return@launch
            }
            
            // Check permissions
            if (!permissionManager.hasAllPermissions(this@MainActivity)) {
                // Could show a permission reminder or navigate to settings
                Timber.w("Some permissions are missing")
            }
        }
    }
    
    private fun setupUI(savedInstanceState: Bundle?) {
        // Setup bottom navigation if needed
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        
        // Setup initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, com.mindguard.ui.dashboard.DashboardFragment())
                .commit()
        }
    }
    
    private fun observeViewModel() {
        // Observe any navigation events or UI state changes
    }
    
    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Handle deep links or notification intents
        intent.let {
            when (it.getStringExtra("open_tab")) {
                "dashboard" -> {
                    // Navigate to dashboard tab
                }
                "focus" -> {
                    // Navigate to focus mode
                }
                "settings" -> {
                    // Navigate to settings
                }
            }
        }
    }
}
