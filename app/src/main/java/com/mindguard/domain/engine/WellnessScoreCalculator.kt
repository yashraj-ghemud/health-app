package com.mindguard.domain.engine

import com.mindguard.data.model.AppCategory
import com.mindguard.data.repository.UsageEventRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WellnessScoreCalculator @Inject constructor(
    private val usageEventRepository: UsageEventRepository
) {
    
    suspend fun calculateWellnessScore(date: String): Int {
        val totalTime = usageEventRepository.getTotalScreenTime(date) ?: 0
        val productiveTime = usageEventRepository.getProductiveTime(date) ?: 0
        val entertainmentTime = usageEventRepository.getEntertainmentTime(date) ?: 0
        val communicationTime = usageEventRepository.getCommunicationTime(date) ?: 0
        
        return calculateWellnessScore(
            totalTime = totalTime,
            productiveTime = productiveTime,
            entertainmentTime = entertainmentTime,
            communicationTime = communicationTime,
            date = date
        )
    }
    
    fun calculateWellnessScore(
        totalTime: Int,
        productiveTime: Int,
        entertainmentTime: Int,
        communicationTime: Int,
        date: String
    ): Int {
        var score = 100
        
        // Productive time ratio (40% weight)
        val productiveRatio = if (totalTime > 0) productiveTime.toFloat() / totalTime else 0f
        val productiveScore = (productiveRatio * 40).toInt()
        score += productiveScore - 20 // Adjust baseline
        
        // Entertainment time penalty (30% weight)
        val entertainmentPenalty = when {
            entertainmentTime > 240 -> -30 // More than 4 hours
            entertainmentTime > 180 -> -20 // More than 3 hours
            entertainmentTime > 120 -> -10 // More than 2 hours
            entertainmentTime > 60 -> -5   // More than 1 hour
            else -> 0
        }
        score += entertainmentPenalty
        
        // Total screen time consideration (20% weight)
        val screenTimePenalty = when {
            totalTime > 600 -> -20 // More than 10 hours
            totalTime > 480 -> -15 // More than 8 hours
            totalTime > 360 -> -10 // More than 6 hours
            totalTime > 240 -> -5  // More than 4 hours
            else -> 0
        }
        score += screenTimePenalty
        
        // Late night usage bonus/penalty (10% weight)
        val lateNightPenalty = if (hasLateNightUsageSync(date)) -10 else 0
        score += lateNightPenalty
        
        // Communication balance bonus (up to 10 points)
        val communicationBonus = when {
            communicationTime in 30..120 -> 10 // Balanced communication
            communicationTime in 15..30 -> 5   // Light communication
            communicationTime in 120..180 -> 5 // Heavy but reasonable
            else -> 0
        }
        score += communicationBonus
        
        return score.coerceIn(0, 100)
    }
    
    private suspend fun hasLateNightUsage(date: String): Boolean {
        // Check for usage between 11 PM and 6 AM
        val events = usageEventRepository.getEventsForDate(date)
        
        return events.any { event ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = event.startTimestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            hour >= 23 || hour < 6
        }
    }
    
    private suspend fun hasLateNightUsageSync(date: String): Boolean {
        // Check for usage between 11 PM and 6 AM
        val events = usageEventRepository.getEventsForDate(date)
        
        return events.any { event ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = event.startTimestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            hour >= 23 || hour < 6
        }
    }
    
    suspend fun getWellnessInsights(date: String): WellnessInsights {
        val score = calculateWellnessScore(date)
        val totalTime = usageEventRepository.getTotalScreenTime(date) ?: 0
        val productiveTime = usageEventRepository.getProductiveTime(date) ?: 0
        val entertainmentTime = usageEventRepository.getEntertainmentTime(date) ?: 0
        
        val insights = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        // Generate insights based on usage patterns
        when {
            entertainmentTime > 180 -> {
                insights.add("High entertainment usage detected")
                recommendations.add("Try setting daily limits for entertainment apps")
            }
            productiveTime > 240 -> {
                insights.add("Excellent productive time")
                recommendations.add("Keep up the great work!")
            }
            totalTime > 480 -> {
                insights.add("High total screen time")
                recommendations.add("Consider taking regular breaks from your device")
            }
            productiveTime < 60 -> {
                insights.add("Low productive time")
                recommendations.add("Schedule dedicated time for deep work activities")
            }
        }
        
        if (hasLateNightUsage(date)) {
            insights.add("Late night device usage detected")
            recommendations.add("Consider establishing a digital curfew before bedtime")
        }
        
        val category = when {
            score >= 80 -> WellnessCategory.EXCELLENT
            score >= 60 -> WellnessCategory.GOOD
            score >= 40 -> WellnessCategory.FAIR
            else -> WellnessCategory.POOR
        }
        
        return WellnessInsights(
            score = score,
            category = category,
            insights = insights,
            recommendations = recommendations
        )
    }
    
    data class WellnessInsights(
        val score: Int,
        val category: WellnessCategory,
        val insights: List<String>,
        val recommendations: List<String>
    )
    
    enum class WellnessCategory(val displayName: String, val color: String) {
        EXCELLENT("Excellent", "#4CAF50"),
        GOOD("Good", "#8BC34A"),
        FAIR("Fair", "#FF9800"),
        POOR("Poor", "#F44336")
    }
}
