package com.mindguard.data.repository

import com.mindguard.data.db.UsageRuleDao
import com.mindguard.data.model.UsageRule
import com.mindguard.data.model.AppCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRuleRepository @Inject constructor(
    private val usageRuleDao: UsageRuleDao
) {
    
    fun getEnabledRules(): Flow<List<UsageRule>> = usageRuleDao.getEnabledRules()
    
    fun getAllRules(): Flow<List<UsageRule>> = usageRuleDao.getAllRules()
    
    suspend fun getAllRulesSync(): List<UsageRule> = usageRuleDao.getAllRulesSync()
    
    suspend fun getRuleById(id: String): UsageRule? {
        return usageRuleDao.getRuleById(id)
    }
    
    suspend fun getRulesForCategory(category: AppCategory): List<UsageRule> {
        return usageRuleDao.getRulesForCategory(category.name)
    }
    
    suspend fun getRulesForPackage(packageName: String): List<UsageRule> {
        return usageRuleDao.getRulesForPackage(packageName)
    }
    
    suspend fun insertRule(rule: UsageRule) {
        usageRuleDao.insertRule(rule)
    }
    
    suspend fun insertRules(rules: List<UsageRule>) {
        usageRuleDao.insertRules(rules)
    }
    
    suspend fun updateRule(rule: UsageRule) {
        usageRuleDao.updateRule(rule)
    }
    
    suspend fun updateRuleEnabled(id: String, enabled: Boolean) {
        usageRuleDao.updateRuleEnabled(id, enabled)
    }
    
    suspend fun deleteRule(rule: UsageRule) {
        usageRuleDao.deleteRule(rule)
    }
    
    suspend fun deleteRuleById(id: String) {
        usageRuleDao.deleteRuleById(id)
    }
    
    suspend fun getRulesForApp(packageName: String, category: AppCategory): List<UsageRule> {
        val packageRules = getRulesForPackage(packageName)
        val categoryRules = getRulesForCategory(category)
        return (packageRules + categoryRules).distinctBy { it.id }
    }
}
