package com.mindguard.domain.engine

import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.TriggerType
import com.mindguard.data.model.UsageRule
import com.mindguard.data.repository.UsageRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RulesEngine @Inject constructor(
    private val usageRuleRepository: UsageRuleRepository
) {
    
    data class RuleEvaluation(
        val packageName: String,
        val category: AppCategory,
        val continuousMinutes: Int,
        val cumulativeMinutes: Int,
        val currentTime: Long
    )
    
    data class TriggeredRule(
        val rule: UsageRule,
        val triggeredValue: Int, // The actual value that triggered the rule
        val triggerTime: Long
    )
    
    suspend fun evaluateRules(
        packageName: String,
        category: AppCategory,
        continuousMinutes: Int,
        cumulativeMinutes: Int
    ): List<TriggeredRule> {
        val applicableRules = getApplicableRules(packageName, category)
        val currentTime = System.currentTimeMillis()
        val triggeredRules = mutableListOf<TriggeredRule>()
        
        for (rule in applicableRules) {
            if (!rule.isEnabled) continue
            
            val evaluation = RuleEvaluation(
                packageName = packageName,
                category = category,
                continuousMinutes = continuousMinutes,
                cumulativeMinutes = cumulativeMinutes,
                currentTime = currentTime
            )
            
            if (isRuleTriggered(rule, evaluation)) {
                val triggerValue = when (rule.triggerType) {
                    TriggerType.CONTINUOUS -> continuousMinutes
                    TriggerType.CUMULATIVE_DAILY -> cumulativeMinutes
                }
                
                triggeredRules.add(
                    TriggeredRule(
                        rule = rule,
                        triggeredValue = triggerValue,
                        triggerTime = currentTime
                    )
                )
                
                Timber.d("Rule triggered: ${rule.id} for $packageName - $triggerValue minutes")
            }
        }
        
        return triggeredRules
    }
    
    private suspend fun getApplicableRules(packageName: String, category: AppCategory): List<UsageRule> {
        // Get rules specific to this package
        val packageRules = usageRuleRepository.getRulesForPackage(packageName)
        
        // Get rules for this category
        val categoryRules = usageRuleRepository.getRulesForCategory(category)
        
        // Get general rules (no specific package or category)
        val generalRules = usageRuleRepository.getAllRules()
            .map { it }
            .filter { rule -> rule.targetCategory == null && rule.targetPackage == null }
        
        // Combine and deduplicate by rule ID
        return (packageRules + categoryRules + generalRules)
            .distinctBy { it.id }
            .filter { it.isEnabled }
    }
    
    private fun isRuleTriggered(rule: UsageRule, evaluation: RuleEvaluation): Boolean {
        // Check time-based restrictions (sleep hours, work hours, etc.)
        if (!isTimeApplicable(rule, evaluation.currentTime)) {
            return false
        }
        
        // Check if the rule applies to the current app/category
        if (!isTargetApplicable(rule, evaluation)) {
            return false
        }
        
        // Check the trigger condition
        return when (rule.triggerType) {
            TriggerType.CONTINUOUS -> {
                evaluation.continuousMinutes >= rule.thresholdMinutes
            }
            TriggerType.CUMULATIVE_DAILY -> {
                evaluation.cumulativeMinutes >= rule.thresholdMinutes
            }
        }
    }
    
    private fun isTimeApplicable(rule: UsageRule, currentTime: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Sleep Guard rules (11pm - 6am)
        if (rule.id.contains("sleep_guard") || rule.id.contains("night")) {
            return currentHour >= 23 || currentHour < 6
        }
        
        // Work hours specific rules
        if (rule.id.contains("work_hours")) {
            return currentHour in 9..17 // 9am to 5pm
        }
        
        // Weekend specific rules
        if (rule.id.contains("weekend")) {
            return currentDayOfWeek == Calendar.SATURDAY || currentDayOfWeek == Calendar.SUNDAY
        }
        
        // Weekday specific rules
        if (rule.id.contains("weekday")) {
            return currentDayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
        }
        
        // Most rules apply all the time
        return true
    }
    
    private fun isTargetApplicable(rule: UsageRule, evaluation: RuleEvaluation): Boolean {
        // If rule targets a specific package, check if it matches
        rule.targetPackage?.let { targetPkg ->
            if (evaluation.packageName != targetPkg) {
                return false
            }
        }
        
        // If rule targets a specific category, check if it matches
        rule.targetCategory?.let { targetCategory ->
            if (evaluation.category != targetCategory) {
                return false
            }
        }
        
        // If neither package nor category is specified, rule applies to all apps
        return true
    }
    
    fun getEnabledRules(): Flow<List<UsageRule>> {
        return usageRuleRepository.getEnabledRules()
    }
    
    suspend fun getRuleById(id: String): UsageRule? {
        return usageRuleRepository.getRuleById(id)
    }
    
    suspend fun addCustomRule(rule: UsageRule) {
        usageRuleRepository.insertRule(rule)
    }
    
    suspend fun updateRule(rule: UsageRule) {
        usageRuleRepository.updateRule(rule)
    }
    
    suspend fun deleteRule(ruleId: String) {
        usageRuleRepository.deleteRuleById(ruleId)
    }
    
    suspend fun toggleRule(ruleId: String, enabled: Boolean) {
        usageRuleRepository.updateRuleEnabled(ruleId, enabled)
    }
    
    // Rule validation
    fun validateRule(rule: UsageRule): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (rule.thresholdMinutes <= 0) {
            errors.add("Threshold must be greater than 0")
        }
        
        if (rule.actionType == com.mindguard.data.model.ActionType.BLOCK && rule.blockDurationMinutes <= 0) {
            errors.add("Block duration must be greater than 0 for block actions")
        }
        
        if (rule.targetPackage == null && rule.targetCategory == null) {
            // This is okay - applies to all apps
        }
        
        if (rule.targetPackage != null && rule.targetCategory != null) {
            // This is also okay - more specific targeting
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val messages: List<String>) : ValidationResult()
    }
    
    // Rule suggestions based on usage patterns
    suspend fun suggestRules(packageName: String, category: AppCategory, averageDailyMinutes: Int): List<UsageRule> {
        val suggestions = mutableListOf<UsageRule>()
        
        when (category) {
            AppCategory.PASSIVE_ENTERTAINMENT -> {
                if (averageDailyMinutes > 60) {
                    suggestions.add(
                        UsageRule(
                            id = "suggested_${packageName}_30min",
                            targetPackage = packageName,
                            targetCategory = null,
                            triggerType = TriggerType.CONTINUOUS,
                            thresholdMinutes = 30,
                            actionType = com.mindguard.data.model.ActionType.BLOCK,
                            blockDurationMinutes = 5,
                            isEnabled = false,
                            strictLevel = com.mindguard.data.model.StrictLevel.BALANCED
                        )
                    )
                }
                
                if (averageDailyMinutes > 120) {
                    suggestions.add(
                        UsageRule(
                            id = "suggested_${packageName}_60min_daily",
                            targetPackage = packageName,
                            targetCategory = null,
                            triggerType = TriggerType.CUMULATIVE_DAILY,
                            thresholdMinutes = 60,
                            actionType = com.mindguard.data.model.ActionType.BLOCK,
                            blockDurationMinutes = 10,
                            isEnabled = false,
                            strictLevel = com.mindguard.data.model.StrictLevel.BALANCED
                        )
                    )
                }
            }
            
            AppCategory.PASSIVE_SCROLL -> {
                if (averageDailyMinutes > 45) {
                    suggestions.add(
                        UsageRule(
                            id = "suggested_${packageName}_45min",
                            targetPackage = packageName,
                            targetCategory = null,
                            triggerType = TriggerType.CONTINUOUS,
                            thresholdMinutes = 45,
                            actionType = com.mindguard.data.model.ActionType.NOTIFY,
                            blockDurationMinutes = 0,
                            isEnabled = false,
                            strictLevel = com.mindguard.data.model.StrictLevel.GENTLE
                        )
                    )
                }
            }
            
            else -> {
                // No suggestions for other categories
            }
        }
        
        return suggestions
    }
}
