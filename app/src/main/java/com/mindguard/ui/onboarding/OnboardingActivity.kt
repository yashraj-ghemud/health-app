package com.mindguard.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mindguard.databinding.ActivityOnboardingBinding
import com.mindguard.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    
    @Inject
    lateinit var permissionRequestHandler: com.mindguard.utils.PermissionRequestHandler
    
    private val viewModel: OnboardingViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupPermissionHandler()
        observeViewModel()
    }
    
    private fun setupViewPager() {
        adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // Empty tab indicators
        }.attach()
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtonVisibility(position)
            }
        })
        
        binding.btnNext.setOnClickListener {
            handleNextClick()
        }
        
        binding.btnSkip.setOnClickListener {
            navigateToMain()
        }
    }
    
    private fun setupPermissionHandler() {
        permissionRequestHandler.registerWithActivity(this)
        permissionRequestHandler.setOnPermissionResult { permission, granted ->
            viewModel.onPermissionResult(permission, granted)
        }
    }
    
    private fun observeViewModel() {
        viewModel.navigateToMain.observe(this) {
            if (it) {
                navigateToMain()
            }
        }
        
        viewModel.currentPage.observe(this) { position ->
            binding.viewPager.currentItem = position
            updateButtonVisibility(position)
        }
        
        viewModel.showPermissionDialog.observe(this) { permission ->
            // Show dialog explaining why permission is needed
            showPermissionExplanationDialog(permission)
        }
    }
    
    private fun updateButtonVisibility(position: Int) {
        val isLastPage = position == adapter.itemCount - 1
        
        binding.btnNext.text = if (isLastPage) "Get Started" else "Next"
        binding.btnSkip.visibility = if (isLastPage) android.view.View.GONE else android.view.View.VISIBLE
        
        // Update button states based on current fragment
        val currentFragment = adapter.getFragment(position)
        if (currentFragment is PermissionFragment) {
            val hasPermission = currentFragment.hasPermission()
            binding.btnNext.isEnabled = hasPermission
        } else {
            binding.btnNext.isEnabled = true
        }
    }
    
    private fun handleNextClick() {
        val currentPosition = binding.viewPager.currentItem
        
        if (currentPosition < adapter.itemCount - 1) {
            val currentFragment = adapter.getFragment(currentPosition)
            
            if (currentFragment is PermissionFragment) {
                if (!currentFragment.hasPermission()) {
                    currentFragment.requestPermission()
                    return
                }
            }
            
            binding.viewPager.currentItem = currentPosition + 1
        } else {
            // Last page - complete onboarding
            viewModel.completeOnboarding()
        }
    }
    
    private fun showPermissionExplanationDialog(permission: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(getPermissionExplanation(permission))
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionForCurrentFragment()
            }
            .setNegativeButton("Skip", null)
            .show()
    }
    
    private fun getPermissionExplanation(permission: String): String {
        return when (permission) {
            "Usage Stats" -> "This permission allows MindGuard to track which apps you use and for how long. Without it, the app cannot monitor your digital wellness."
            "System Overlay" -> "This permission allows MindGuard to show intervention overlays over distracting apps. Without it, the app cannot help you break unhealthy habits."
            "Notifications" -> "This permission allows MindGuard to send you reminders and daily summaries. Without it, you'll miss important insights about your digital habits."
            "Call Log" -> "This permission allows MindGuard to track phone call duration for communication analytics. This is optional and can be skipped."
            else -> "This permission is required for MindGuard to function properly."
        }
    }
    
    private fun requestPermissionForCurrentFragment() {
        val currentPosition = binding.viewPager.currentItem
        val currentFragment = adapter.getFragment(currentPosition)
        
        if (currentFragment is PermissionFragment) {
            currentFragment.requestPermission()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
    }
}
