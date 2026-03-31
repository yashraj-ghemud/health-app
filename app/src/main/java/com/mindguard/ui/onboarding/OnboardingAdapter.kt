package com.mindguard.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(private val activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    private val fragmentFactories: List<() -> Fragment> = listOf(
        { WelcomeFragment() },
        { MissionFragment() },
        { PermissionUsageStatsFragment() },
        { PermissionOverlayFragment() },
        { PermissionNotificationFragment() },
        { PermissionCallLogFragment() },
        { ProfileSetupFragment() },
        { AppReviewFragment() }
    )
    
    override fun getItemCount(): Int = fragmentFactories.size
    
    override fun createFragment(position: Int): Fragment {
        return fragmentFactories[position]()
    }
    
    fun getFragment(position: Int): Fragment? {
        val itemId = getItemId(position)
        return activity.supportFragmentManager.findFragmentByTag("f$itemId")
    }
}
