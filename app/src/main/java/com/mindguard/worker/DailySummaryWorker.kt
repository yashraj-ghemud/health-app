package com.mindguard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindguard.data.model.DailySummary
import com.mindguard.data.repository.DailySummaryRepository
import com.mindguard.data.repository.UsageEventRepository
import com.mindguard.domain.engine.WellnessScoreCalculator
import com.mindguard.domain.engine.AchievementEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageEventRepository: UsageEventRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val focusSessionRepository: com.mindguard.data.repository.FocusSessionRepository,
    private val wellnessScoreCalculator: WellnessScoreCalculator,
    private val achievementEngine: AchievementEngine
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val yesterday = getYesterdayDateString()
            generateDailySummary(yesterday)
            checkAchievements(yesterday)
            
            Timber.d("Daily summary generated for $yesterday")
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating daily summary")
            Result.failure()
        }
    }
    
    private suspend fun generateDailySummary(date: String) {
        // Get usage data for the day
        val totalScreenTime = usageEventRepository.getTotalScreenTime(date) ?: 0
        val productiveTime = usageEventRepository.getProductiveTime(date) ?: 0
        val entertainmentTime = usageEventRepository.getEntertainmentTime(date) ?: 0
        val communicationTime = usageEventRepository.getCommunicationTime(date) ?: 0
        
        // Calculate wellness score
        val wellnessScore = wellnessScoreCalculator.calculateWellnessScore(
            totalTime = totalScreenTime,
            productiveTime = productiveTime,
            entertainmentTime = entertainmentTime,
            communicationTime = communicationTime,
            date = date
        )
        
        // Count interventions (this would require querying intervention logs)
        val interventionsCount = getInterventionsCount(date)
        
        // Count focus sessions
        val focusSessionsCount = getFocusSessionsCount(date)
        val focusSessionsMinutes = getFocusSessionsMinutes(date)
        
        // Calculate streak
        val streakDays = calculateStreakDays(date)
        
        // Create daily summary
        val summary = DailySummary(
            date = date,
            wellnessScore = wellnessScore,
            totalScreenMinutes = totalScreenTime,
            productiveMinutes = productiveTime,
            entertainmentMinutes = entertainmentTime,
            communicationMinutes = communicationTime,
            interventionsCount = interventionsCount,
            focusSessionsCount = focusSessionsCount,
            focusSessionsMinutes = focusSessionsMinutes,
            streakDays = streakDays
        )
        
        // Save to database
        dailySummaryRepository.upsertSummary(summary)
        
        Timber.d("Generated daily summary for $date: score=$wellnessScore, screenTime=$totalScreenTime")
    }
    
    private suspend fun checkAchievements(date: String) {
        // Check daily achievements
        achievementEngine.checkDailyAchievements(date)
        
        // Check night owl achievements
        achievementEngine.checkNightOwlAchievements(date)
        
        // Check streak achievements
        val summary = dailySummaryRepository.getSummaryForDate(date)
        summary?.let {
            achievementEngine.checkStreakAchievements(it.streakDays)
        }
    }
    
    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    private suspend fun getInterventionsCount(date: String): Int {
        // This would query the intervention log table
        // For now, return a placeholder value
        return 0
    }
    
    private suspend fun getFocusSessionsCount(date: String): Int {
        return try {
            focusSessionRepository.getCompletedSessionsCount(date)
        } catch (e: Exception) {
            Timber.w(e, "Error getting focus sessions count")
            0
        }
    }
    
    private suspend fun getFocusSessionsMinutes(date: String): Int {
        return try {
            focusSessionRepository.getTotalFocusMinutes(date)
        } catch (e: Exception) {
            Timber.w(e, "Error getting focus sessions minutes")
            0
        }
    }
    
    private suspend fun calculateStreakDays(date: String): Int {
        return try {
            dailySummaryRepository.getCurrentStreak(date)
        } catch (e: Exception) {
            Timber.w(e, "Error calculating streak days")
            0
        }
    }
}
