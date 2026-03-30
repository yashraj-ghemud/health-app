package com.mindguard.di

import android.content.Context
import androidx.room.Room
import com.mindguard.data.db.AppDatabase
import com.mindguard.data.db.Converters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        callback: AppDatabase.Callback
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mindguard_database"
        )
        .addTypeConverter(Converters())
        .addCallback(callback)
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUsageEventDao(database: AppDatabase) = database.usageEventDao()
    
    @Provides
    fun provideUsageRuleDao(database: AppDatabase) = database.usageRuleDao()
    
    @Provides
    fun provideAchievementDao(database: AppDatabase) = database.achievementDao()
    
    @Provides
    fun provideDailySummaryDao(database: AppDatabase) = database.dailySummaryDao()
    
    @Provides
    fun provideFocusSessionDao(database: AppDatabase) = database.focusSessionDao()
    
    @Provides
    fun provideAppCategoryMappingDao(database: AppDatabase) = database.appCategoryMappingDao()
}
