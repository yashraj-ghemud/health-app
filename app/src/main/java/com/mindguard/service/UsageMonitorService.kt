package com.mindguard.service

import android.app.*
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mindguard.R
import com.mindguard.data.model.AppCategory
import com.mindguard.data.repository.AppCategoryRepository
import com.mindguard.data.repository.UsageRuleRepository
import com.mindguard.domain.engine.RulesEngine
import com.mindguard.intervention.InterventionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class UsageMonitorService : Service() {
    
    @Inject
    lateinit var sessionTracker: SessionTracker
    
    @Inject
    lateinit var appCategoryRepository: AppCategoryRepository
    
    @Inject
    lateinit var usageRuleRepository: UsageRuleRepository
    
    @Inject
    lateinit var rulesEngine: RulesEngine
    
    @Inject
    lateinit var interventionManager: InterventionManager
    
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var powerManager: PowerManager
    private lateinit var packageManager: PackageManager
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    private var flushJob: Job? = null
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "usage_monitor_channel"
        const val POLL_INTERVAL_SECONDS = 30L
        const val FLUSH_INTERVAL_MINUTES = 5L
        
        fun startService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        packageManager = packageManager
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        Timber.d("UsageMonitorService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_MONITORING" -> startMonitoring()
            "STOP_MONITORING" -> stopMonitoring()
            else -> startMonitoring()
        }
        
        return START_STICKY
    }
    
    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) {
            Timber.d("Monitoring already active")
            return
        }
        
        Timber.d("Starting usage monitoring")
        
        serviceScope.launch {
            sessionTracker.startTracking()
            startMonitoringLoop()
            startFlushLoop()
        }
    }
    
    private fun stopMonitoring() {
        Timber.d("Stopping usage monitoring")
        
        monitoringJob?.cancel()
        flushJob?.cancel()
        
        serviceScope.launch {
            sessionTracker.stopTracking()
            sessionTracker.forceFlushAllSessions()
        }
    }
    
    private fun startMonitoringLoop() {
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    if (!powerManager.isInteractive) {
                        // Screen is off, skip polling but continue tracking
                        delay(TimeUnit.SECONDS.toMillis(POLL_INTERVAL_SECONDS))
                        continue
                    }
                    
                    val currentApp = getCurrentForegroundApp()
                    if (currentApp != null) {
                        sessionTracker.updateForegroundApp(currentApp.packageName, currentApp.appLabel)
                        
                        // Process tick for session tracking
                        sessionTracker.processTick()
                        
                        // Check rules and trigger interventions if needed
                        checkRulesAndIntervene(currentApp.packageName)
                    } else {
                        // No foreground app (likely on launcher)
                        sessionTracker.updateForegroundApp(null)
                    }
                    
                    delay(TimeUnit.SECONDS.toMillis(POLL_INTERVAL_SECONDS))
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error in monitoring loop")
                    delay(TimeUnit.SECONDS.toMillis(POLL_INTERVAL_SECONDS))
                }
            }
        }
    }
    
    private fun startFlushLoop() {
        flushJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Force flush sessions every 5 minutes to ensure data persistence
                    sessionTracker.forceFlushAllSessions()
                    delay(TimeUnit.MINUTES.toMillis(FLUSH_INTERVAL_MINUTES))
                } catch (e: Exception) {
                    Timber.e(e, "Error in flush loop")
                    delay(TimeUnit.MINUTES.toMillis(FLUSH_INTERVAL_MINUTES))
                }
            }
        }
    }
    
    private suspend fun getCurrentForegroundApp(): ForegroundAppInfo? {
        return try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.MINUTES.toMillis(1)
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            if (usageStats.isNullOrEmpty()) {
                return null
            }
            
            // Find the app with the most recent last used time
            val sortedStats = usageStats.filter { it.lastTimeUsed > 0 }
                .sortedByDescending { it.lastTimeUsed }
            
            if (sortedStats.isEmpty()) {
                return null
            }
            
            val mostRecentStats = sortedStats.first()
            val packageName = mostRecentStats.packageName
            
            // Skip system apps and launcher
            if (isSystemApp(packageName) || isLauncher(packageName)) {
                return null
            }
            
            val appLabel = getAppLabel(packageName)
            
            ForegroundAppInfo(
                packageName = packageName,
                appLabel = appLabel,
                lastTimeUsed = mostRecentStats.lastTimeUsed,
                totalTimeInForeground = mostRecentStats.totalTimeInForeground
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error getting foreground app")
            null
        }
    }
    
    private suspend fun checkRulesAndIntervene(packageName: String) {
        try {
            val category = appCategoryRepository.getCategoryForPackage(packageName)
            val continuousMinutes = sessionTracker.getContinuousMinutes(packageName)
            val cumulativeMinutes = sessionTracker.getCumulativeMinutes(packageName)
            
            val triggeredRules = rulesEngine.evaluateRules(
                packageName = packageName,
                category = category,
                continuousMinutes = continuousMinutes,
                cumulativeMinutes = cumulativeMinutes
            )
            
            triggeredRules.forEach { triggeredRule ->
                when (triggeredRule.rule.actionType) {
                    com.mindguard.data.model.ActionType.BLOCK -> {
                        interventionManager.showBlockOverlay(
                            packageName = packageName,
                            rule = triggeredRule.rule,
                            durationMinutes = triggeredRule.rule.blockDurationMinutes
                        )
                    }
                    com.mindguard.data.model.ActionType.NOTIFY -> {
                        interventionManager.showNotification(
                            packageName = packageName,
                            rule = triggeredRule.rule
                        )
                    }
                    com.mindguard.data.model.ActionType.OVERLAY -> {
                        interventionManager.showMotivationalOverlay(
                            packageName = packageName,
                            rule = triggeredRule.rule
                        )
                    }
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking rules for $packageName")
        }
    }
    
    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }
    
    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }
    
    private fun getAppLabel(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Usage Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "MindGuard is protecting your focus"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, com.mindguard.ui.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MindGuard Active")
            .setContentText("Protecting your digital wellness")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        Timber.d("UsageMonitorService destroyed")
        
        monitoringJob?.cancel()
        flushJob?.cancel()
        
        try {
            // Stop tracking before cancelling the scope
            kotlinx.coroutines.runBlocking {
                sessionTracker.stopTracking()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error stopping session tracker")
        }
        
        serviceScope.cancel()
    }
    
    data class ForegroundAppInfo(
        val packageName: String,
        val appLabel: String,
        val lastTimeUsed: Long,
        val totalTimeInForeground: Long
    )
}
