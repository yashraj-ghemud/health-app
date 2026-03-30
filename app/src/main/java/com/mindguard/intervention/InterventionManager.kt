package com.mindguard.intervention

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mindguard.R
import com.mindguard.data.model.ActionType
import com.mindguard.data.model.UsageRule
import com.mindguard.service.BlocklistManager
import com.mindguard.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blocklistManager: BlocklistManager,
    private val quoteRepository: QuoteRepository
) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val packageManager = context.packageManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentOverlayView: InterventionOverlayView? = null
    private var currentPackageName: String? = null
    
    private val _isOverlayShowing = MutableStateFlow(false)
    val isOverlayShowing: StateFlow<Boolean> = _isOverlayShowing
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "intervention_notifications"
        private const val NOTIFICATION_ID_BASE = 2000
    }
    
    init {
        createNotificationChannel()
    }
    
    fun showBlockOverlay(
        packageName: String,
        rule: UsageRule,
        durationMinutes: Int
    ) {
        managerScope.launch {
            try {
                // Add to blocklist first
                blocklistManager.addToBlocklist(
                    packageName = packageName,
                    durationMinutes = durationMinutes,
                    reason = "Rule: ${rule.id}",
                    ruleId = rule.id
                )
                
                // Log the intervention
                logIntervention(
                    ruleId = rule.id,
                    packageName = packageName,
                    blockDurationMinutes = durationMinutes,
                    actionType = ActionType.BLOCK
                )
                
                // Show overlay
                val appLabel = getAppLabel(packageName)
                val quote = quoteRepository.getRandomQuote()
                
                showOverlay(
                    packageName = packageName,
                    appLabel = appLabel,
                    rule = rule,
                    durationMinutes = durationMinutes,
                    quote = quote
                )
                
                Timber.d("Block overlay shown for $packageName for $durationMinutes minutes")
                
            } catch (e: Exception) {
                Timber.e(e, "Error showing block overlay for $packageName")
                // Fallback to notification if overlay fails
                showNotification(packageName, rule)
            }
        }
    }
    
    fun showMotivationalOverlay(
        packageName: String,
        rule: UsageRule
    ) {
        managerScope.launch {
            try {
                val appLabel = getAppLabel(packageName)
                val quote = quoteRepository.getRandomQuote()
                
                showOverlay(
                    packageName = packageName,
                    appLabel = appLabel,
                    rule = rule,
                    durationMinutes = 0, // No timer for motivational overlay
                    quote = quote
                )
                
                logIntervention(
                    ruleId = rule.id,
                    packageName = packageName,
                    blockDurationMinutes = 0,
                    actionType = ActionType.OVERLAY
                )
                
                Timber.d("Motivational overlay shown for $packageName")
                
            } catch (e: Exception) {
                Timber.e(e, "Error showing motivational overlay for $packageName")
                showNotification(packageName, rule)
            }
        }
    }
    
    fun showNotification(
        packageName: String,
        rule: UsageRule
    ) {
        managerScope.launch {
            try {
                val appLabel = getAppLabel(packageName)
                val notificationId = NOTIFICATION_ID_BASE + rule.id.hashCode()
                
                // Create intent to open main app
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("open_tab", "dashboard")
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("MindGuard Reminder")
                    .setContentText(getNotificationMessage(packageName, appLabel, rule))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(packageName, appLabel, rule)))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .build()
                
                notificationManager.notify(notificationId, notification)
                
                logIntervention(
                    ruleId = rule.id,
                    packageName = packageName,
                    blockDurationMinutes = 0,
                    actionType = ActionType.NOTIFY
                )
                
                Timber.d("Notification shown for $packageName")
                
            } catch (e: Exception) {
                Timber.e(e, "Error showing notification for $packageName")
            }
        }
    }
    
    private fun showOverlay(
        packageName: String,
        appLabel: String,
        rule: UsageRule,
        durationMinutes: Int,
        quote: String
    ) {
        // Dismiss any existing overlay
        dismissOverlay()
        
        currentPackageName = packageName
        
        // Create overlay view
        currentOverlayView = InterventionOverlayView(context).apply {
            setOnDismissListener {
                onOverlayDismissed()
            }
            
            setOnBypassListener { bypassedPackageName ->
                onOverlayBypassed(bypassedPackageName)
            }
            
            showIntervention(
                packageName = packageName,
                appLabel = appLabel,
                rule = rule,
                durationMinutes = durationMinutes,
                quote = quote
            )
        }
        
        // Add overlay to window
        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
            )
            
            params.gravity = Gravity.CENTER
            windowManager.addView(currentOverlayView, params)
            _isOverlayShowing.value = true
            
        } catch (e: Exception) {
            Timber.e(e, "Error adding overlay view")
            currentOverlayView = null
            throw e
        }
    }
    
    fun dismissOverlay() {
        currentOverlayView?.let { overlay ->
            try {
                windowManager.removeView(overlay)
                overlay.dismiss()
            } catch (e: Exception) {
                Timber.e(e, "Error removing overlay view")
            } finally {
                currentOverlayView = null
                currentPackageName = null
                _isOverlayShowing.value = false
            }
        }
    }
    
    private fun onOverlayDismissed() {
        currentOverlayView = null
        currentPackageName = null
        _isOverlayShowing.value = false
    }
    
    private fun onOverlayBypassed(packageName: String) {
        managerScope.launch {
            // Remove from blocklist
            blocklistManager.removeFromBlocklist(packageName)
            
            // Log bypass
            // This would update the intervention log with bypass information
            
            // Show toast
            Toast.makeText(
                context,
                "Bypass used for ${getAppLabel(packageName)}",
                Toast.LENGTH_SHORT
            ).show()
            
            dismissOverlay()
        }
    }
    
    suspend fun checkAndShowOverlay(packageName: String): Boolean {
        // Check if app is blocked and show overlay if needed
        val isBlocked = blocklistManager.isBlocked(packageName)
        if (isBlocked && currentPackageName != packageName) {
            val blockInfo = blocklistManager.getBlockInfo(packageName)
            blockInfo?.let { info ->
                val remainingMinutes = ((info.unblockTime - System.currentTimeMillis()) / (60 * 1000)).toInt()
                if (remainingMinutes > 0) {
                    // Find the rule that caused this block
                    val rule = getDefaultBlockRule()
                    showBlockOverlay(
                        packageName = packageName,
                        rule = rule,
                        durationMinutes = remainingMinutes
                    )
                    return true
                }
            }
        }
        return false
    }
    
    private fun logIntervention(
        ruleId: String,
        packageName: String,
        blockDurationMinutes: Int,
        actionType: ActionType
    ) {
        managerScope.launch(Dispatchers.IO) {
            try {
                // This would create and save an InterventionLog entity
                // For now, just log to Timber
                Timber.d("Intervention logged: rule=$ruleId, package=$packageName, duration=$blockDurationMinutes, action=$actionType")
            } catch (e: Exception) {
                Timber.e(e, "Error logging intervention")
            }
        }
    }
    
    private fun getAppLabel(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
    private fun getNotificationMessage(
        packageName: String,
        appLabel: String,
        rule: UsageRule
    ): String {
        return when (rule.actionType) {
            ActionType.BLOCK -> {
                "You've been using $appLabel for a while. Consider taking a break."
            }
            ActionType.NOTIFY -> {
                "Time for a break from $appLabel?"
            }
            ActionType.OVERLAY -> {
                "Consider your digital wellness with $appLabel"
            }
        }
    }
    
    private fun getDefaultBlockRule(): UsageRule {
        return UsageRule(
            id = "default_block",
            targetCategory = null,
            targetPackage = null,
            triggerType = com.mindguard.data.model.TriggerType.CONTINUOUS,
            thresholdMinutes = 30,
            actionType = ActionType.BLOCK,
            blockDurationMinutes = 5,
            isEnabled = true,
            strictLevel = com.mindguard.data.model.StrictLevel.BALANCED
        )
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Intervention Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for digital wellness interventions"
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
}
