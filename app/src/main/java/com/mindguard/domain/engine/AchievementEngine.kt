package com.mindguard.domain.engine

import com.mindguard.data.model.Achievement
import com.mindguard.data.model.AchievementCategory
import com.mindguard.data.repository.AchievementRepository
import com.mindguard.data.repository.DailySummaryRepository
import com.mindguard.data.repository.FocusSessionRepository
import com.mindguard.data.repository.UsageEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementEngine @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val usageEventRepository: UsageEventRepository
) {
    
    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    suspend fun checkFocusSessionAchievements(
        sessionDurationMinutes: Int,
        callback: (List<Achievement>) -> Unit = {}
    ) {
        engineScope.launch {
            val unlockedAchievements = mutableListOf<Achievement>()
            
            try {
                // Check first focus session
                if (!achievementRepository.isAchievementUnlocked("first_focus_session")) {
                    val totalSessions = focusSessionRepository.getAllSessionsSync().size
                    if (totalSessions >= 1) {
                        unlockAchievement("first_focus_session")
                        unlockedAchievements.add(getAchievement("first_focus_session"))
                    }
                }
                
                // Check focus duration achievements
                when {
                    sessionDurationMinutes >= 120 && !achievementRepository.isAchievementUnlocked("focus_2_hours") -> {
                        unlockAchievement("focus_2_hours")
                        unlockedAchievements.add(getAchievement("focus_2_hours"))
                    }
                    sessionDurationMinutes >= 60 && !achievementRepository.isAchievementUnlocked("focus_1_hour") -> {
                        unlockAchievement("focus_1_hour")
                        unlockedAchievements.add(getAchievement("focus_1_hour"))
                    }
                    sessionDurationMinutes >= 45 && !achievementRepository.isAchievementUnlocked("focus_45_minutes") -> {
                        unlockAchievement("focus_45_minutes")
                        unlockedAchievements.add(getAchievement("focus_45_minutes"))
                    }
                }
                
                // Check total focus time achievements
                val weeklyStats = focusSessionRepository.getWeeklyStats()
                if (weeklyStats.second >= 300 && !achievementRepository.isAchievementUnlocked("weekly_focus_5_hours")) { // 5 hours = 300 minutes
                    unlockAchievement("weekly_focus_5_hours")
                    unlockedAchievements.add(getAchievement("weekly_focus_5_hours"))
                }
                
                if (weeklyStats.second >= 600 && !achievementRepository.isAchievementUnlocked("weekly_focus_10_hours")) { // 10 hours = 600 minutes
                    unlockAchievement("weekly_focus_10_hours")
                    unlockedAchievements.add(getAchievement("weekly_focus_10_hours"))
                }
                
                callback(unlockedAchievements)
                
            } catch (e: Exception) {
                Timber.e(e, "Error checking focus session achievements")
            }
        }
    }
    
    suspend fun checkDailyAchievements(date: String) {
        engineScope.launch {
            try {
                val summary = dailySummaryRepository.getSummaryForDate(date) ?: return@launch
                val unlockedAchievements = mutableListOf<Achievement>()
                
                // Check wellness score achievements
                when {
                    summary.wellnessScore >= 90 && !achievementRepository.isAchievementUnlocked("wellness_perfect_day") -> {
                        unlockAchievement("wellness_perfect_day")
                        unlockedAchievements.add(getAchievement("wellness_perfect_day"))
                    }
                    summary.wellnessScore >= 80 && !achievementRepository.isAchievementUnlocked("wellness_excellent_day") -> {
                        unlockAchievement("wellness_excellent_day")
                        unlockedAchievements.add(getAchievement("wellness_excellent_day"))
                    }
                }
                
                // Check productive time achievements
                when {
                    summary.productiveMinutes >= 240 && !achievementRepository.isAchievementUnlocked("productive_4_hours") -> {
                        unlockAchievement("productive_4_hours")
                        unlockedAchievements.add(getAchievement("productive_4_hours"))
                    }
                    summary.productiveMinutes >= 180 && !achievementRepository.isAchievementUnlocked("productive_3_hours") -> {
                        unlockAchievement("productive_3_hours")
                        unlockedAchievements.add(getAchievement("productive_3_hours"))
                    }
                }
                
                // Check entertainment control achievements
                if (summary.entertainmentMinutes <= 30 && !achievementRepository.isAchievementUnlocked("entertainment_under_30")) {
                    unlockAchievement("entertainment_under_30")
                    unlockedAchievements.add(getAchievement("entertainment_under_30"))
                }
                
                if (summary.entertainmentMinutes <= 60 && !achievementRepository.isAchievementUnlocked("entertainment_under_60")) {
                    unlockAchievement("entertainment_under_60")
                    unlockedAchievements.add(getAchievement("entertainment_under_60"))
                }
                
                // Check app-specific achievements
                checkAppSpecificAchievements(date, unlockedAchievements)
                
                if (unlockedAchievements.isNotEmpty()) {
                    Timber.d("Unlocked ${unlockedAchievements.size} daily achievements")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error checking daily achievements")
            }
        }
    }
    
    private suspend fun checkAppSpecificAchievements(date: String, unlockedAchievements: MutableList<Achievement>) {
        val topApps = usageEventRepository.getTopAppsForDate(date, 10)
        
        // Check Instagram-free day
        val instagramUsage = topApps.find { it.packageName == "com.instagram.android" }
        if (instagramUsage == null && !achievementRepository.isAchievementUnlocked("instagram_free_day")) {
            unlockAchievement("instagram_free_day")
            unlockedAchievements.add(getAchievement("instagram_free_day"))
        }
        
        // Check TikTok-free day
        val tiktokUsage = topApps.find { it.packageName == "com.tiktok" }
        if (tiktokUsage == null && !achievementRepository.isAchievementUnlocked("tiktok_free_day")) {
            unlockAchievement("tiktok_free_day")
            unlockedAchievements.add(getAchievement("tiktok_free_day"))
        }
        
        // Check YouTube control
        val youtubeUsage = topApps.find { it.packageName == "com.google.android.youtube" }
        if (youtubeUsage != null && youtubeUsage.totalMinutes <= 30 && !achievementRepository.isAchievementUnlocked("youtube_under_30")) {
            unlockAchievement("youtube_under_30")
            unlockedAchievements.add(getAchievement("youtube_under_30"))
        }
    }
    
    suspend fun checkStreakAchievements(currentStreak: Int) {
        engineScope.launch {
            try {
                val unlockedAchievements = mutableListOf<Achievement>()
                
                when {
                    currentStreak >= 30 && !achievementRepository.isAchievementUnlocked("streak_30_days") -> {
                        unlockAchievement("streak_30_days")
                        unlockedAchievements.add(getAchievement("streak_30_days"))
                    }
                    currentStreak >= 14 && !achievementRepository.isAchievementUnlocked("streak_14_days") -> {
                        unlockAchievement("streak_14_days")
                        unlockedAchievements.add(getAchievement("streak_14_days"))
                    }
                    currentStreak >= 7 && !achievementRepository.isAchievementUnlocked("7_day_streak") -> {
                        unlockAchievement("7_day_streak")
                        unlockedAchievements.add(getAchievement("7_day_streak"))
                    }
                    currentStreak >= 3 && !achievementRepository.isAchievementUnlocked("3_day_streak") -> {
                        unlockAchievement("3_day_streak")
                        unlockedAchievements.add(getAchievement("3_day_streak"))
                    }
                }
                
                if (unlockedAchievements.isNotEmpty()) {
                    Timber.d("Unlocked ${unlockedAchievements.size} streak achievements")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error checking streak achievements")
            }
        }
    }
    
    suspend fun checkMilestoneAchievements() {
        engineScope.launch {
            try {
                val unlockedAchievements = mutableListOf<Achievement>()
                
                // Check total time saved achievements
                val lastWeekSummaries = dailySummaryRepository.getSummariesForDateRange(
                    getDateString(-7), getDateString(0)
                )
                val totalEntertainmentTime = lastWeekSummaries.sumOf { it.entertainmentMinutes }
                
                when {
                    totalEntertainmentTime <= 300 && !achievementRepository.isAchievementUnlocked("saved_5_hours_weekly") -> { // 5 hours = 300 minutes
                        unlockAchievement("saved_5_hours_weekly")
                        unlockedAchievements.add(getAchievement("saved_5_hours_weekly"))
                    }
                    totalEntertainmentTime <= 600 && !achievementRepository.isAchievementUnlocked("saved_10_hours_weekly") -> { // 10 hours = 600 minutes
                        unlockAchievement("saved_10_hours_weekly")
                        unlockedAchievements.add(getAchievement("saved_10_hours_weekly"))
                    }
                }
                
                // Check total achievements unlocked
                val totalUnlocked = achievementRepository.getUnlockedCount()
                when {
                    totalUnlocked >= 20 && !achievementRepository.isAchievementUnlocked("achievement_collector") -> {
                        unlockAchievement("achievement_collector")
                        unlockedAchievements.add(getAchievement("achievement_collector"))
                    }
                    totalUnlocked >= 10 && !achievementRepository.isAchievementUnlocked("achievement_enthusiast") -> {
                        unlockAchievement("achievement_enthusiast")
                        unlockedAchievements.add(getAchievement("achievement_enthusiast"))
                    }
                }
                
                if (unlockedAchievements.isNotEmpty()) {
                    Timber.d("Unlocked ${unlockedAchievements.size} milestone achievements")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error checking milestone achievements")
            }
        }
    }
    
    suspend fun checkNightOwlAchievements(date: String) {
        engineScope.launch {
            try {
                val events = usageEventRepository.getEventsForDateSync(date)
                val hasLateNightUsage = events.any { event ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = event.startTimestamp
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    hour >= 23 || hour < 6
                }
                
                if (!hasLateNightUsage && !achievementRepository.isAchievementUnlocked("night_owl_slayer")) {
                    unlockAchievement("night_owl_slayer")
                    Timber.d("Unlocked Night Owl Slayer achievement")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error checking night owl achievements")
            }
        }
    }
    
    private suspend fun unlockAchievement(achievementKey: String) {
        achievementRepository.unlockAchievement(achievementKey)
        Timber.d("Achievement unlocked: $achievementKey")
    }
    
    private suspend fun getAchievement(achievementKey: String): Achievement {
        return achievementRepository.getAchievementByKey(achievementKey)
            ?: Achievement(
                achievementKey = achievementKey,
                title = "Unknown Achievement",
                description = "Achievement description not found",
                iconRes = "ic_trophy",
                unlockedAt = System.currentTimeMillis(),
                isNew = true,
                category = AchievementCategory.MILESTONE
            )
    }
    
    private fun getDateString(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAgo)
        return dateFormat.format(calendar.time)
    }
    
    suspend fun getAchievementProgress(): AchievementProgress {
        val totalAchievements = achievementRepository.getAllAchievementsSync().size
        val unlockedAchievements = achievementRepository.getUnlockedCount()
        val newAchievements = achievementRepository.getNewAchievements()
        
        return AchievementProgress(
            total = totalAchievements,
            unlocked = unlockedAchievements,
            new = newAchievements.size,
            percentage = if (totalAchievements > 0) (unlockedAchievements * 100) / totalAchievements else 0
        )
    }
    
    data class AchievementProgress(
        val total: Int,
        val unlocked: Int,
        val new: Int,
        val percentage: Int
    )
}
