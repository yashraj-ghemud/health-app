package com.mindguard.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mindguard.R
import com.mindguard.utils.PermissionManager

abstract class PermissionFragment : Fragment() {
    
    protected abstract val permissionName: String
    protected abstract val permissionDescription: String
    protected abstract val importanceLevel: String
    
    private var tvPermissionName: TextView? = null
    private var tvPermissionDescription: TextView? = null
    private var tvImportance: TextView? = null
    private var tvStatus: TextView? = null
    private var ivStatusIcon: ImageView? = null
    private var btnGrant: Button? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_onboarding_permission, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tvPermissionName = view.findViewById(R.id.tvPermissionName)
        tvPermissionDescription = view.findViewById(R.id.tvPermissionDescription)
        tvImportance = view.findViewById(R.id.tvImportance)
        tvStatus = view.findViewById(R.id.tvStatus)
        ivStatusIcon = view.findViewById(R.id.ivStatusIcon)
        btnGrant = view.findViewById(R.id.btnGrant)
        
        setupUI()
    }
    
    private fun setupUI() {
        tvPermissionName?.text = permissionName
        tvPermissionDescription?.text = permissionDescription
        tvImportance?.text = importanceLevel
        
        updatePermissionStatus()
        
        btnGrant?.setOnClickListener {
            requestPermission()
        }
    }
    
    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }
    
    private fun updatePermissionStatus() {
        val granted = hasPermission()
        if (granted) {
            tvStatus?.text = "Granted"
            ivStatusIcon?.setImageResource(R.drawable.ic_check_circle)
            btnGrant?.visibility = View.GONE
        } else {
            tvStatus?.text = "Not Granted"
            ivStatusIcon?.setImageDrawable(null)
            btnGrant?.visibility = View.VISIBLE
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        tvPermissionName = null
        tvPermissionDescription = null
        tvImportance = null
        tvStatus = null
        ivStatusIcon = null
        btnGrant = null
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
