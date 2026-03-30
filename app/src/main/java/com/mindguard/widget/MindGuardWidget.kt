package com.mindguard.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.mindguard.R
import com.mindguard.ui.MainActivity
import com.mindguard.worker.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MindGuardWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Schedule widget updates
        val scheduler = WorkManagerScheduler(context)
        scheduler.scheduleWidgetUpdateWorker()
    }
    
    override fun onDisabled(context: Context) {
        // Cancel widget updates
        val scheduler = WorkManagerScheduler(context)
        scheduler.cancelWidgetUpdateWorker()
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, MindGuardWidget::class.java)
                )
                
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
    
    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.mindguard.UPDATE_WIDGET"
        
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val widgetData = getWidgetData(context)
                    
                    withContext(Dispatchers.Main) {
                        val views = RemoteViews(context.packageName, R.layout.widget_mind_guard)
                        
                        // Update small widget
                        if (appWidgetManager.getAppWidgetInfo(appWidgetId)?.let {
                            it.minWidth < 100 && it.minHeight < 100
                        } == true) {
                            updateSmallWidget(views, widgetData, context)
                        } else {
                            updateMediumWidget(views, widgetData, context)
                        }
                        
                        // Create click intent
                        val intent = Intent(context, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                        
                        // Update the widget
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                    
                } catch (e: Exception) {
                    // Handle error by showing default state
                    val views = RemoteViews(context.packageName, R.layout.widget_mind_guard)
                    views.setTextViewText(R.id.wellness_score, "--")
                    views.setTextViewText(R.id.status_text, "Loading...")
                    
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
        
        private suspend fun getWidgetData(context: Context): WidgetData {
            // This would query the actual data from repositories
            // For now, return mock data
            return WidgetData(
                wellnessScore = 75,
                totalScreenTime = 240, // 4 hours
                status = "On Track",
                topApps = listOf(
                    WidgetApp("Instagram", 45, Color.RED),
                    WidgetApp("WhatsApp", 30, Color.BLUE),
                    WidgetApp("YouTube", 25, Color.RED)
                )
            )
        }
        
        private fun updateSmallWidget(
            views: RemoteViews,
            data: WidgetData,
            context: Context
        ) {
            views.setTextViewText(R.id.wellness_score, "75")
            views.setTextViewText(R.id.status_text, "Healthy")
            
            views.setTextColor(R.id.wellness_score, Color.GREEN)
            
            // Show only wellness score and status for small widget
            views.setViewVisibility(R.id.top_apps_container, View.GONE)
            views.setViewVisibility(R.id.screen_time_container, View.GONE)
        }
        
        private fun updateMediumWidget(
            views: RemoteViews,
            data: WidgetData,
            context: Context
        ) {
            // Wellness score
            views.setTextViewText(R.id.wellness_score, "75")
            views.setTextViewText(R.id.status_text, "Healthy Habits")
            
            views.setTextColor(R.id.wellness_score, Color.GREEN)
            
            // Screen time
            views.setTextViewText(R.id.screen_time_text, "4h 20m")
            
            // Top apps
            views.setViewVisibility(R.id.top_apps_container, View.VISIBLE)
            
            views.setTextViewText(R.id.app1_name, "Instagram")
            views.setTextViewText(R.id.app1_time, "45m")
            views.setTextColor(R.id.app1_name, Color.RED)
            
            views.setTextViewText(R.id.app2_name, "WhatsApp")
            views.setTextViewText(R.id.app2_time, "30m")
            views.setTextColor(R.id.app2_name, Color.BLUE)
            
            views.setViewVisibility(R.id.app3_name, View.GONE)
            
            views.setViewVisibility(R.id.screen_time_container, View.VISIBLE)
        }
        
        private fun formatScreenTime(minutes: Int): String {
            return when {
                minutes >= 60 -> {
                    val hours = minutes / 60
                    val remainingMinutes = minutes % 60
                    if (remainingMinutes > 0) {
                        "${hours}h ${remainingMinutes}m"
                    } else {
                        "${hours}h"
                    }
                }
                else -> "${minutes}m"
            }
        }
    }
}

data class WidgetData(
    val wellnessScore: Int,
    val totalScreenTime: Int,
    val status: String,
    val topApps: List<WidgetApp>
)

data class WidgetApp(
    val name: String,
    val minutes: Int,
    val color: Int
)
