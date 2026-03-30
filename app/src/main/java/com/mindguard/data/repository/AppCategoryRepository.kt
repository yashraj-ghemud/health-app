package com.mindguard.data.repository

import com.mindguard.data.db.AppCategoryMappingDao
import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.AppCategoryMapping
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategoryRepository @Inject constructor(
    private val appCategoryMappingDao: AppCategoryMappingDao
) {
    
    fun getAllMappings(): Flow<List<AppCategoryMapping>> = appCategoryMappingDao.getAllMappings()
    
    suspend fun getCategoryForPackage(packageName: String): AppCategory {
        val mapping = appCategoryMappingDao.getMappingForPackage(packageName)
        return mapping?.category ?: AppCategory.NEUTRAL
    }
    
    suspend fun updateCategoryForPackage(packageName: String, category: AppCategory) {
        appCategoryMappingDao.updateCategoryForPackage(packageName, category.name)
    }
    
    suspend fun addCustomMapping(packageName: String, category: AppCategory) {
        val mapping = AppCategoryMapping(
            packageName = packageName,
            category = category,
            isUserDefined = true
        )
        appCategoryMappingDao.insertMapping(mapping)
    }
    
    suspend fun removeCustomMapping(packageName: String) {
        appCategoryMappingDao.deleteMappingByPackage(packageName)
    }
    
    suspend fun getUserDefinedMappings(): List<AppCategoryMapping> {
        return appCategoryMappingDao.getUserDefinedMappings()
    }
    
    suspend fun getPreDefinedMappings(): List<AppCategoryMapping> {
        return appCategoryMappingDao.getPreDefinedMappings()
    }
    
    fun getMappingsForCategory(category: AppCategory): Flow<List<AppCategoryMapping>> {
        return appCategoryMappingDao.getMappingsForCategoryFlow(category.name)
    }
    
    suspend fun resetToDefaults() {
        appCategoryMappingDao.deleteUserDefinedMappings()
    }
    
    suspend fun getAppsInCategory(category: AppCategory): List<String> {
        val mappings = appCategoryMappingDao.getMappingsForCategorySync(category.name)
        return mappings.filter { it.category == category }
            .map { it.packageName }
    }
    
    suspend fun isAppInCategory(packageName: String, category: AppCategory): Boolean {
        val mapping = appCategoryMappingDao.getMappingForPackage(packageName)
        return mapping?.category == category
    }
    
    suspend fun getCategoryCount(category: AppCategory): Int {
        return appCategoryMappingDao.getCountForCategory(category.name)
    }
}
