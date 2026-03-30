package com.mindguard.data.db

import androidx.room.*
import com.mindguard.data.model.UsageRule
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageRuleDao {
    
    @Query("SELECT * FROM usage_rules WHERE isEnabled = 1 ORDER BY createdAt")
    fun getEnabledRules(): Flow<List<UsageRule>>
    
    @Query("SELECT * FROM usage_rules ORDER BY createdAt")
    fun getAllRules(): Flow<List<UsageRule>>
    
    @Query("SELECT * FROM usage_rules ORDER BY createdAt")
    suspend fun getAllRulesSync(): List<UsageRule>
    
    @Query("SELECT * FROM usage_rules WHERE id = :id")
    suspend fun getRuleById(id: String): UsageRule?
    
    @Query("SELECT * FROM usage_rules WHERE targetCategory = :category AND isEnabled = 1")
    suspend fun getRulesForCategory(category: String): List<UsageRule>
    
    @Query("SELECT * FROM usage_rules WHERE targetPackage = :packageName AND isEnabled = 1")
    suspend fun getRulesForPackage(packageName: String): List<UsageRule>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: UsageRule)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<UsageRule>)
    
    @Update
    suspend fun updateRule(rule: UsageRule)
    
    @Query("UPDATE usage_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateRuleEnabled(id: String, enabled: Boolean)
    
    @Delete
    suspend fun deleteRule(rule: UsageRule)
    
    @Query("DELETE FROM usage_rules WHERE id = :id")
    suspend fun deleteRuleById(id: String)
}
