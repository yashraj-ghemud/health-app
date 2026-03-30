package com.mindguard.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    private val fragments = mutableListOf<Fragment>()
    
    init {
        // Initialize with all onboarding fragments
        fragments.addAll(
            listOf(
                WelcomeFragment(),
                MissionFragment(),
                PermissionUsageStatsFragment(),
                PermissionOverlayFragment(),
                PermissionNotificationFragment(),
                PermissionCallLogFragment(),
                ProfileSetupFragment(),
                AppReviewFragment()
            )
        )
    }
    
    override fun getItemCount(): Int = fragments.size
    
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
    
    fun getFragment(position: Int): Fragment {
        return fragments[position]
    }
}
