package com.mindguard.ui.dashboard.viewholders

import com.mindguard.data.model.AppCategory

data class DashboardItem(
    val type: DashboardItemType,
    val data: Any
)

enum class DashboardItemType {
    WELLNESS_SCORE,
    TIME_BREAKDOWN,
    TIMELINE,
    APP_LEADERBOARD,
    STREAK_ACHIEVEMENTS,
    WEEKLY_TREND
}

// Data classes for each dashboard item type

data class WellnessScoreData(
    val score: Int,
    val date: String,
    val trend: String
)

data class TimeBreakdownData(
    val categories: Map<AppCategory, Int>,
    val totalMinutes: Int
)

data class TimelineData(
    val date: String,
    val blocks: List<TimelineBlock>
)

data class TimelineBlock(
    val packageName: String,
    val appLabel: String,
    val category: AppCategory,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int
)

data class AppUsageData(
    val packageName: String,
    val appLabel: String,
    val category: AppCategory,
    val totalMinutes: Int,
    val isOverLimit: Boolean
)

data class StreakAchievementsData(
    val currentStreak: Int,
    val bestStreak: Int,
    val unlockedAchievements: Int,
    val totalAchievements: Int,
    val recentAchievements: List<com.mindguard.data.model.Achievement>
)

data class WeeklyTrendData(
    val dailyData: List<DailyTrendData>,
    val averageProductive: Int,
    val averageEntertainment: Int,
    val averageWellnessScore: Int
)

data class DailyTrendData(
    val date: String,
    val productiveMinutes: Int,
    val entertainmentMinutes: Int,
    val wellnessScore: Int
)
