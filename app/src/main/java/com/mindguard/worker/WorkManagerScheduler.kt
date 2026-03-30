package com.mindguard.worker

import android.content.Context
import androidx.work.*
import com.mindguard.MindGuardApplication
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleAllWorkers() {
        scheduleDailySummaryWorker()
        scheduleSleepGuardWorker()
        scheduleBehaviorAnalysisWorker()
    }
    
    private fun scheduleDailySummaryWorker() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            1, TimeUnit.DAYS
        ).apply {
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            setInitialDelay(calculateDelayUntil9PM(), TimeUnit.MILLISECONDS)
            setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
        }.build()
        
        workManager.enqueueUniquePeriodicWork(
            "DailySummaryWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }
    
    private fun scheduleSleepGuardWorker() {
        // Schedule multiple sleep guard checks during sleep hours
        val sleepChecks = listOf(23, 0, 2, 4) // 11 PM, 12 AM, 2 AM, 4 AM
        
        sleepChecks.forEach { hour ->
            val sleepWorkRequest = OneTimeWorkRequestBuilder<SleepGuardWorker>()
                .setInitialDelay(calculateDelayUntilHour(hour), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresCharging(false)
                        .build()
                )
                .addTag("SleepGuard")
                .build()
            
            workManager.enqueueUniqueWork(
                "SleepGuard_$hour",
                ExistingWorkPolicy.REPLACE,
                sleepWorkRequest
            )
        }
    }
    
    private fun scheduleBehaviorAnalysisWorker() {
        // Run behavior analysis weekly on Sunday at 10 AM
        val weeklyWorkRequest = PeriodicWorkRequestBuilder<BehaviorAnalysisWorker>(
            7, TimeUnit.DAYS
        ).apply {
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    .build()
            )
            setInitialDelay(calculateDelayUntilSunday10AM(), TimeUnit.MILLISECONDS)
            setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
        }.build()
        
        workManager.enqueueUniquePeriodicWork(
            "BehaviorAnalysisWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            weeklyWorkRequest
        )
    }
    
    private fun calculateDelayUntil9PM(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 21)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If it's already past 9 PM, schedule for tomorrow
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar.timeInMillis - now
    }
    
    private fun calculateDelayUntilHour(targetHour: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, targetHour)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If it's already past the target hour, schedule for tomorrow
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar.timeInMillis - now
    }
    
    private fun calculateDelayUntilSunday10AM(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 10)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If it's already past Sunday 10 AM, schedule for next Sunday
            if (timeInMillis <= now) {
                add(java.util.Calendar.WEEK_OF_YEAR, 1)
            }
        }
        
        return calendar.timeInMillis - now
    }
    
    fun cancelAllWorkers() {
        workManager.cancelAllWorkByTag("SleepGuard")
        workManager.cancelUniqueWork("DailySummaryWorker")
        workManager.cancelUniqueWork("BehaviorAnalysisWorker")
        workManager.cancelUniqueWork("WidgetUpdateWorker")
    }
    
    fun scheduleWidgetUpdateWorker() {
        val widgetWorkRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            15, TimeUnit.MINUTES
        ).addTag("WidgetUpdate").build()
        
        workManager.enqueueUniquePeriodicWork(
            "WidgetUpdateWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            widgetWorkRequest
        )
    }
    
    fun cancelWidgetUpdateWorker() {
        workManager.cancelUniqueWork("WidgetUpdateWorker")
    }
    
    fun rescheduleWorkers() {
        cancelAllWorkers()
        scheduleAllWorkers()
    }
    
    fun getWorkerStatuses(): WorkerStatuses {
        val dailySummaryWork = workManager.getWorkInfosForUniqueWorkLiveData("DailySummaryWorker").value
        val behaviorAnalysisWork = workManager.getWorkInfosForUniqueWorkLiveData("BehaviorAnalysisWorker").value
        val sleepGuardWorks = workManager.getWorkInfosByTagLiveData("SleepGuard").value
        
        return WorkerStatuses(
            dailySummaryWorker = dailySummaryWork?.firstOrNull()?.let {
                WorkerStatus(
                    name = "Daily Summary",
                    state = it.state.name,
                    isRunning = it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                )
            },
            behaviorAnalysisWorker = behaviorAnalysisWork?.firstOrNull()?.let {
                WorkerStatus(
                    name = "Behavior Analysis",
                    state = it.state.name,
                    isRunning = it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                )
            },
            sleepGuardWorkers = sleepGuardWorks?.map { work ->
                WorkerStatus(
                    name = "Sleep Guard",
                    state = work.state.name,
                    isRunning = work.state == WorkInfo.State.RUNNING || work.state == WorkInfo.State.ENQUEUED
                )
            } ?: emptyList()
        )
    }
}

data class WorkerStatus(
    val name: String,
    val state: String,
    val isRunning: Boolean
)

data class WorkerStatuses(
    val dailySummaryWorker: WorkerStatus?,
    val behaviorAnalysisWorker: WorkerStatus?,
    val sleepGuardWorkers: List<WorkerStatus>
)
