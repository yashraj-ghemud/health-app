package com.mindguard.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mindguard.utils.PermissionManager

abstract class PermissionFragment : Fragment() {
    
    protected abstract val permissionName: String
    protected abstract val permissionDescription: String
    protected abstract val importanceLevel: String
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create a basic layout for permission fragments
        return createPermissionLayout()
    }
    
    private fun createPermissionLayout(): View {
        // This would be replaced with proper XML layout inflation
        // For now, creating a simple view programmatically
        return View(requireContext()).apply {
            // Set up basic permission UI
        }
    }
    
    abstract fun hasPermission(): Boolean
    
    abstract fun requestPermission()
    
    protected fun openPermissionSettings(intent: Intent) {
        startActivity(intent)
    }
    
    protected fun getPermissionManager(): PermissionManager {
        return PermissionManager()
    }
}

class PermissionUsageStatsFragment : PermissionFragment() {
    
    override val permissionName = "Usage Stats"
    override val permissionDescription = "Required to track which apps you use and for how long"
    override val importanceLevel = "Essential"
    
    override fun hasPermission(): Boolean {
        return getPermissionManager().hasUsageStatsPermission(requireContext())
    }
    
    override fun requestPermission() {
        val intent = getPermissionManager().getUsageStatsIntent()
        openPermissionSettings(intent)
    }
}

class PermissionOverlayFragment : PermissionFragment() {
    
    override val permissionName = "System Overlay"
    override val permissionDescription = "Required to show intervention overlays over other apps"
    override val importanceLevel = "Essential"
    
    override fun hasPermission(): Boolean {
        return getPermissionManager().hasOverlayPermission(requireContext())
    }
    
    override fun requestPermission() {
        val intent = getPermissionManager().getOverlayIntent(requireContext())
        openPermissionSettings(intent)
    }
}

class PermissionNotificationFragment : PermissionFragment() {
    
    override val permissionName = "Notifications"
    override val permissionDescription = "Required to send you reminders and daily summaries"
    override val importanceLevel = "Important"
    
    override fun hasPermission(): Boolean {
        return getPermissionManager().hasNotificationPermission(requireContext())
    }
    
    override fun requestPermission() {
        val intent = getPermissionManager().getNotificationIntent(requireContext())
        openPermissionSettings(intent)
    }
}

class PermissionCallLogFragment : PermissionFragment() {
    
    override val permissionName = "Call Log"
    override val permissionDescription = "Required to track phone call duration for communication analytics"
    override val importanceLevel = "Optional"
    
    override fun hasPermission(): Boolean {
        return getPermissionManager().hasCallLogPermission(requireContext())
    }
    
    override fun requestPermission() {
        val intent = getPermissionManager().getCallLogIntent(requireContext())
        openPermissionSettings(intent)
    }
}
