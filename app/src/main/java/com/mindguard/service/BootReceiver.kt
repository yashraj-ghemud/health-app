package com.mindguard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mindguard.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Timber.d("Boot/replacement event received: ${intent.action}")
                
                // Check if we have all required permissions before starting
                if (permissionManager.hasAllPermissions(context)) {
                    Timber.d("All permissions granted, starting usage monitor service")
                    UsageMonitorService.startService(context)
                } else {
                    Timber.w("Missing permissions, not starting service automatically")
                }
            }
        }
    }
}
