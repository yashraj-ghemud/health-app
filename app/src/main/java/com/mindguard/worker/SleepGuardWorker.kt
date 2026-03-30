package com.mindguard.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindguard.R
import com.mindguard.domain.engine.AchievementEngine
import com.mindguard.intervention.InterventionManager
import com.mindguard.service.UsageMonitorService
import com.mindguard.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.*

@HiltWorker
class SleepGuardWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val interventionManager: InterventionManager,
    private val achievementEngine: AchievementEngine
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "SleepGuardWorker"
        const val NOTIFICATION_CHANNEL_ID = "sleep_guard_channel"
        const val NOTIFICATION_ID = 4001
        
        // Sleep hours (11 PM to 6 AM by default)
        const val SLEEP_START_HOUR = 23
        const val SLEEP_END_HOUR = 6
    }
    
    override suspend fun doWork(): Result {
        return try {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            
            if (isInSleepHours(currentHour)) {
                checkLateNightUsage()
                sendSleepReminderIfNeeded(currentHour)
            }
            
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Error in SleepGuardWorker")
            Result.failure()
        }
    }
    
    private suspend fun checkLateNightUsage() {
        try {
            // Check if user has been using phone during sleep hours
            val hasLateNightUsage = checkForLateNightUsage()
            
            if (hasLateNightUsage) {
                sendSleepIntervention()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking late night usage")
        }
    }
    
    private suspend fun checkForLateNightUsage(): Boolean {
        // This would check usage events for the current sleep period
        // For now, return a placeholder logic
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        
        // Simulate checking - in real implementation, this would query UsageEventRepository
        // for events between SLEEP_START_HOUR and current time
        return when (currentHour) {
            in 23..23 -> currentMinute > 30 // After 11:30 PM
            in 0..2 -> true // Early morning hours
            in 3..5 -> currentMinute < 30 // Before 6:30 AM
            else -> false
        }
    }
    
    private suspend fun sendSleepIntervention() {
        try {
            // Show a gentle sleep reminder overlay
            val sleepRule = com.mindguard.data.model.UsageRule(
                id = "sleep_guard_reminder",
                targetCategory = null,
                targetPackage = null,
                triggerType = com.mindguard.data.model.TriggerType.CONTINUOUS,
                thresholdMinutes = 0,
                actionType = com.mindguard.data.model.ActionType.OVERLAY,
                blockDurationMinutes = 0,
                isEnabled = true,
                strictLevel = com.mindguard.data.model.StrictLevel.GENTLE
            )
            
            interventionManager.showMotivationalOverlay(
                packageName = "system",
                rule = sleepRule
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error sending sleep intervention")
        }
    }
    
    private fun sendSleepReminderIfNeeded(currentHour: Int) {
        try {
            when (currentHour) {
                SLEEP_START_HOUR -> {
                    // Send initial sleep reminder at 11 PM
                    sendSleepNotification(
                        title = "Time to Wind Down",
                        message = "It's 11 PM. Consider putting your phone away for better sleep quality.",
                        isGentle = true
                    )
                }
                SLEEP_START_HOUR + 1 -> {
                    // Send stronger reminder at midnight
                    sendSleepNotification(
                        title = "Late Night Usage Detected",
                        message = "Your brain needs rest. Your wellness score will be affected.",
                        isGentle = false
                    )
                }
                2 -> {
                    // Send early morning reminder
                    sendSleepNotification(
                        title = "Still Awake?",
                        message = "Early morning screen time can disrupt your sleep cycle.",
                        isGentle = true
                    )
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error sending sleep reminder")
        }
    }
    
    private fun sendSleepNotification(title: String, message: String, isGentle: Boolean) {
        createNotificationChannel()
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_tab", "dashboard")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = androidx.core.app.NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isGentle) androidx.core.app.NotificationCompat.PRIORITY_DEFAULT else androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        Timber.d("Sleep notification sent: $title")
    }
    
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sleep Guard",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for sleep time reminders"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun isInSleepHours(hour: Int): Boolean {
        return when {
            hour >= SLEEP_START_HOUR -> true // 11 PM - 11:59 PM
            hour < SLEEP_END_HOUR -> true      // 12 AM - 5:59 AM
            else -> false
        }
    }
}
