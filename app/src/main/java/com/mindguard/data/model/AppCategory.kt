package com.mindguard.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
enum class AppCategory(val displayName: String, val color: String) : Parcelable {
    DEEP_WORK("Deep Work", "#4CAF50"),
    COMMUNICATION("Communication", "#2196F3"),
    PASSIVE_ENTERTAINMENT("Entertainment", "#F44336"),
    PASSIVE_SCROLL("Social Media", "#FF9800"),
    SYSTEM_UTILITY("System", "#9E9E9E"),
    NEUTRAL("Neutral", "#607D8B")
}

@Entity(tableName = "usage_events")
data class UsageEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appLabel: String,
    val category: AppCategory,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val durationMinutes: Int,
    val sessionType: SessionType,
    val date: String // Format: yyyy-MM-dd
)

enum class SessionType {
    CONTINUOUS,
    RESUMED
}

@Entity(tableName = "usage_rules")
data class UsageRule(
    @PrimaryKey
    val id: String,
    val targetCategory: AppCategory?,
    val targetPackage: String?,
    val triggerType: TriggerType,
    val thresholdMinutes: Int,
    val actionType: ActionType,
    val blockDurationMinutes: Int,
    val isEnabled: Boolean,
    val strictLevel: StrictLevel,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TriggerType {
    CONTINUOUS,
    CUMULATIVE_DAILY
}

enum class ActionType {
    BLOCK,
    NOTIFY,
    OVERLAY
}

enum class StrictLevel {
    GENTLE,
    BALANCED,
    STRICT
}

@Entity(tableName = "interventions_log")
data class InterventionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: String,
    val packageName: String,
    val triggeredAt: Long,
    val blockDurationMinutes: Int,
    val bypassUsed: Boolean,
    val bypassReason: String?,
    val completedAt: Long?
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val achievementKey: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val unlockedAt: Long?,
    val isNew: Boolean = false,
    val category: AchievementCategory
)

enum class AchievementCategory {
    FOCUS,
    STREAK,
    PRODUCTIVITY,
    MILESTONE
}

@Entity(tableName = "daily_summary")
data class DailySummary(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val wellnessScore: Int,
    val totalScreenMinutes: Int,
    val productiveMinutes: Int,
    val entertainmentMinutes: Int,
    val communicationMinutes: Int,
    val interventionsCount: Int,
    val focusSessionsCount: Int,
    val focusSessionsMinutes: Int,
    val streakDays: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val durationMinutes: Int,
    val goalLabel: String,
    val completed: Boolean,
    val allowedCategories: List<AppCategory>,
    val blockedPackages: List<String>
)

@Entity(tableName = "app_categories")
data class AppCategoryMapping(
    @PrimaryKey
    val packageName: String,
    val category: AppCategory,
    val isUserDefined: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
