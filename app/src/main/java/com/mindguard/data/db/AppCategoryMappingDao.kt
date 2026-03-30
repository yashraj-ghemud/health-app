package com.mindguard.data.db

import androidx.room.*
import com.mindguard.data.model.AppCategoryMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCategoryMappingDao {
    
    @Query("SELECT * FROM app_categories ORDER BY packageName")
    fun getAllMappings(): Flow<List<AppCategoryMapping>>
    
    @Query("SELECT * FROM app_categories WHERE packageName = :packageName")
    suspend fun getMappingForPackage(packageName: String): AppCategoryMapping?
    
    @Query("SELECT * FROM app_categories WHERE category = :category ORDER BY packageName")
    suspend fun getMappingsForCategory(category: String): List<AppCategoryMapping>
    
    @Query("SELECT * FROM app_categories WHERE category = :category ORDER BY packageName")
    fun getMappingsForCategoryFlow(category: String): Flow<List<AppCategoryMapping>>
    
    @Query("SELECT * FROM app_categories WHERE category = :category ORDER BY packageName")
    suspend fun getMappingsForCategorySync(category: String): List<AppCategoryMapping>
    
    @Query("SELECT * FROM app_categories WHERE isUserDefined = 1 ORDER BY packageName")
    suspend fun getUserDefinedMappings(): List<AppCategoryMapping>
    
    @Query("SELECT * FROM app_categories WHERE isUserDefined = 0 ORDER BY packageName")
    suspend fun getPreDefinedMappings(): List<AppCategoryMapping>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: AppCategoryMapping)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<AppCategoryMapping>)
    
    @Update
    suspend fun updateMapping(mapping: AppCategoryMapping)
    
    @Query("UPDATE app_categories SET category = :category, isUserDefined = 1, lastUpdated = :timestamp WHERE packageName = :packageName")
    suspend fun updateCategoryForPackage(packageName: String, category: String, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteMapping(mapping: AppCategoryMapping)
    
    @Query("DELETE FROM app_categories WHERE packageName = :packageName")
    suspend fun deleteMappingByPackage(packageName: String)
    
    @Query("DELETE FROM app_categories WHERE isUserDefined = 1")
    suspend fun deleteUserDefinedMappings()
    
    @Query("SELECT COUNT(*) FROM app_categories WHERE category = :category")
    suspend fun getCountForCategory(category: String): Int
}
