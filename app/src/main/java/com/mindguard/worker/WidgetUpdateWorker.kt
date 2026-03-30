package com.mindguard.worker

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindguard.widget.MindGuardWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(applicationContext, MindGuardWidget::class.java)
            )
            
            if (appWidgetIds.isNotEmpty()) {
                // Update all widgets
                for (appWidgetId in appWidgetIds) {
                    MindGuardWidget.updateAppWidget(applicationContext, appWidgetManager, appWidgetId)
                }
                
                Timber.d("Updated ${appWidgetIds.size} widgets")
            }
            
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Error updating widgets")
            Result.failure()
        }
    }
}
