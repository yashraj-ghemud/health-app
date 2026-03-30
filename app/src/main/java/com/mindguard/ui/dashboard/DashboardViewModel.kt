package com.mindguard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.data.model.AppCategory
import com.mindguard.data.repository.*
import com.mindguard.domain.engine.WellnessScoreCalculator
import com.mindguard.service.SessionTracker
import com.mindguard.ui.dashboard.viewholders.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageEventRepository: UsageEventRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val achievementRepository: AchievementRepository,
    private val appCategoryRepository: AppCategoryRepository,
    private val sessionTracker: SessionTracker,
    private val wellnessScoreCalculator: WellnessScoreCalculator
) : ViewModel() {
    
    private val _dashboardItems = MutableStateFlow<List<DashboardItem>>(emptyList())
    val dashboardItems: StateFlow<List<DashboardItem>> = _dashboardItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    init {
        refreshDashboardData()
    }
    
    fun refreshDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val today = dateFormat.format(Date())
                val items = mutableListOf<DashboardItem>()
                
                // 1. Wellness Score Card
                val wellnessScore = getWellnessScore(today)
                items.add(
                    DashboardItem(
                        type = DashboardItemType.WELLNESS_SCORE,
                        data = WellnessScoreData(
                            score = wellnessScore,
                            date = displayDateFormat.format(Date()),
                            trend = getWeeklyTrend()
                        )
                    )
                )
                
                // 2. Time Breakdown Donut Chart
                val timeBreakdown = getTimeBreakdown(today)
                val totalMinutes = timeBreakdown.values.sum()
                if (totalMinutes > 0) {
                    items.add(
                        DashboardItem(
                            type = DashboardItemType.TIME_BREAKDOWN,
                            data = TimeBreakdownData(
                                categories = timeBreakdown,
                                totalMinutes = totalMinutes
                            )
                        )
                    )
                }
                
                // 3. Timeline View
                val timelineData = getTimelineData(today)
                items.add(
                    DashboardItem(
                        type = DashboardItemType.TIMELINE,
                        data = timelineData
                    )
                )
                
                // 4. Top Apps Leaderboard
                val topApps = getTopApps(today)
                items.add(
                    DashboardItem(
                        type = DashboardItemType.APP_LEADERBOARD,
                        data = topApps
                    )
                )
                
                // 5. Streak & Achievements
                val streakData = getStreakAndAchievements()
                items.add(
                    DashboardItem(
                        type = DashboardItemType.STREAK_ACHIEVEMENTS,
                        data = streakData
                    )
                )
                
                // 6. Weekly Trend Chart
                val weeklyTrend = getWeeklyTrendData()
                items.add(
                    DashboardItem(
                        type = DashboardItemType.WEEKLY_TREND,
                        data = weeklyTrend
                    )
                )
                
                _dashboardItems.value = items
                
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing dashboard data")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun getWellnessScore(date: String): Int {
        val summary = dailySummaryRepository.getSummaryForDate(date)
        return summary?.wellnessScore ?: wellnessScoreCalculator.calculateWellnessScore(date)
    }
    
    private suspend fun getWeeklyTrend(): String {
        val summaries = dailySummaryRepository.getRecentSummariesSync(7)
        val scores = summaries.map { it.wellnessScore }
        
        return if (scores.size >= 2) {
            val recent = scores.takeLast(3).average()
            val previous = scores.dropLast(3).takeLast(3).average()
            when {
                recent > previous + 5 -> "Improving ↗️"
                recent < previous - 5 -> "Declining ↘️"
                else -> "Stable →"
            }
        } else {
            "New →"
        }
    }
    
    private suspend fun getTimeBreakdown(date: String): Map<AppCategory, Int> {
        val categorySummaries = usageEventRepository.getTimeByCategory(date)
        return categorySummaries.associate { it.category to it.totalMinutes }
    }
    
    private suspend fun getTimelineData(date: String): TimelineData {
        val events = usageEventRepository.getEventsForDate(date).first()
        val timelineBlocks = events.map { event: com.mindguard.data.model.UsageEvent ->
            TimelineBlock(
                packageName = event.packageName,
                appLabel = event.appLabel,
                category = event.category,
                startTime = event.startTimestamp,
                endTime = event.endTimestamp,
                durationMinutes = event.durationMinutes
            )
        }
        
        return TimelineData(
            date = date,
            blocks = timelineBlocks.sortedBy { it.startTime }
        )
    }
    
    private suspend fun getTopApps(date: String): List<AppUsageData> {
        val topApps = usageEventRepository.getTopAppsForDate(date, 5)
        return topApps.map { app ->
            AppUsageData(
                packageName = app.packageName,
                appLabel = app.appLabel,
                category = app.category,
                totalMinutes = app.totalMinutes,
                isOverLimit = isOverLimit(app.category, app.totalMinutes)
            )
        }
    }
    
    private fun isOverLimit(category: AppCategory, minutes: Int): Boolean {
        return when (category) {
            AppCategory.PASSIVE_ENTERTAINMENT -> minutes > 60
            AppCategory.PASSIVE_SCROLL -> minutes > 45
            else -> false
        }
    }
    
    private suspend fun getStreakAndAchievements(): StreakAchievementsData {
        val today = dateFormat.format(Date())
        val currentStreak = dailySummaryRepository.getCurrentStreak(today)
        val bestStreak = dailySummaryRepository.getBestStreak()
        val unlockedCount = achievementRepository.getUnlockedCount()
        val totalCount = achievementRepository.getAllAchievements().first().size
        
        return StreakAchievementsData(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            unlockedAchievements = unlockedCount,
            totalAchievements = totalCount,
            recentAchievements = achievementRepository.getNewAchievements()
        )
    }
    
    private suspend fun getWeeklyTrendData(): WeeklyTrendData {
        val summaries = dailySummaryRepository.getRecentSummaries(7).first()
        
        val dailyData = summaries.map { summary: com.mindguard.data.model.DailySummary ->
            DailyTrendData(
                date = summary.date,
                productiveMinutes = summary.productiveMinutes,
                entertainmentMinutes = summary.entertainmentMinutes,
                wellnessScore = summary.wellnessScore
            )
        }
        
        return WeeklyTrendData(
            dailyData = dailyData,
            averageProductive = if (dailyData.isNotEmpty()) dailyData.map { it.productiveMinutes }.average().toInt() else 0,
            averageEntertainment = if (dailyData.isNotEmpty()) dailyData.map { it.entertainmentMinutes }.average().toInt() else 0,
            averageWellnessScore = if (dailyData.isNotEmpty()) dailyData.map { it.wellnessScore }.average().toInt() else 0
        )
    }
}
