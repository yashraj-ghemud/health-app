package com.mindguard.utils

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class PermissionManager {
    
    companion object {
        const val PACKAGE_USAGE_STATS_PERMISSION = "android.permission.PACKAGE_USAGE_STATS"
        const val SYSTEM_ALERT_WINDOW_PERMISSION = "android.permission.SYSTEM_ALERT_WINDOW"
        const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
        const val READ_CALL_LOG_PERMISSION = "android.permission.READ_CALL_LOG"
        const val FOREGROUND_SERVICE_PERMISSION = "android.permission.FOREGROUND_SERVICE"
        
        const val REQUEST_CODE_USAGE_STATS = 1001
        const val REQUEST_CODE_OVERLAY = 1002
        const val REQUEST_CODE_NOTIFICATIONS = 1003
        const val REQUEST_CODE_CALL_LOG = 1004
    }
    
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(POST_NOTIFICATIONS_PERMISSION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications are automatically granted on older versions
        }
    }
    
    fun hasCallLogPermission(context: Context): Boolean {
        return context.checkSelfPermission(READ_CALL_LOG_PERMISSION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAllPermissions(context: Context): Boolean {
        return hasUsageStatsPermission(context) &&
                hasOverlayPermission(context) &&
                hasNotificationPermission(context) &&
                hasCallLogPermission(context)
    }
    
    fun getUsageStatsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }
    
    fun getOverlayIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
    
    fun getNotificationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }
    
    fun getCallLogIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    
    fun getPermissionStatus(context: Context): Map<String, Boolean> {
        return mapOf(
            "Usage Stats" to hasUsageStatsPermission(context),
            "System Overlay" to hasOverlayPermission(context),
            "Notifications" to hasNotificationPermission(context),
            "Call Log" to hasCallLogPermission(context)
        )
    }
    
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            "Usage Stats" -> "Required to track which apps you use and for how long"
            "System Overlay" -> "Required to show intervention overlays over other apps"
            "Notifications" -> "Required to send you reminders and daily summaries"
            "Call Log" -> "Required to track phone call duration for communication analytics"
            else -> "Unknown permission"
        }
    }
    
    fun getPermissionImportance(permission: String): String {
        return when (permission) {
            "Usage Stats" -> "Essential"
            "System Overlay" -> "Essential"
            "Notifications" -> "Important"
            "Call Log" -> "Optional"
            else -> "Unknown"
        }
    }
}

class PermissionRequestHandler {
    
    private var usageStatsLauncher: ActivityResultLauncher<Intent>? = null
    private var overlayLauncher: ActivityResultLauncher<Intent>? = null
    private var notificationLauncher: ActivityResultLauncher<String>? = null
    private var callLogLauncher: ActivityResultLauncher<String>? = null
    
    private var onPermissionResult: ((String, Boolean) -> Unit)? = null
    
    fun registerWithActivity(activity: AppCompatActivity) {
        usageStatsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onPermissionResult?.invoke("Usage Stats", true) // User returned from settings
        }
        
        overlayLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onPermissionResult?.invoke("System Overlay", true)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                onPermissionResult?.invoke("Notifications", granted)
            }
        }
        
        callLogLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            onPermissionResult?.invoke("Call Log", granted)
        }
    }
    
    fun registerWithFragment(fragment: Fragment) {
        usageStatsLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onPermissionResult?.invoke("Usage Stats", true)
        }
        
        overlayLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onPermissionResult?.invoke("System Overlay", true)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher = fragment.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                onPermissionResult?.invoke("Notifications", granted)
            }
        }
        
        callLogLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            onPermissionResult?.invoke("Call Log", granted)
        }
    }
    
    fun requestUsageStatsPermission(context: Context) {
        val intent = PermissionManager().getUsageStatsIntent()
        usageStatsLauncher?.launch(intent)
    }
    
    fun requestOverlayPermission(context: Context) {
        val intent = PermissionManager().getOverlayIntent(context)
        overlayLauncher?.launch(intent)
    }
    
    fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher?.launch(PermissionManager.POST_NOTIFICATIONS_PERMISSION)
        } else {
            onPermissionResult?.invoke("Notifications", true)
        }
    }
    
    fun requestCallLogPermission(context: Context) {
        callLogLauncher?.launch(PermissionManager.READ_CALL_LOG_PERMISSION)
    }
    
    fun setOnPermissionResult(callback: (String, Boolean) -> Unit) {
        onPermissionResult = callback
    }
}
