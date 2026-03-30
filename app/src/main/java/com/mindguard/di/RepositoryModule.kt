package com.mindguard.di

import com.mindguard.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUsageEventRepository(
        usageEventDao: com.mindguard.data.db.UsageEventDao,
        @ApplicationScope coroutineScope: CoroutineScope
    ): UsageEventRepository {
        return UsageEventRepository(usageEventDao, coroutineScope)
    }
    
    @Provides
    @Singleton
    fun provideUsageRuleRepository(
        usageRuleDao: com.mindguard.data.db.UsageRuleDao
    ): UsageRuleRepository {
        return UsageRuleRepository(usageRuleDao)
    }
    
    @Provides
    @Singleton
    fun provideAchievementRepository(
        achievementDao: com.mindguard.data.db.AchievementDao
    ): AchievementRepository {
        return AchievementRepository(achievementDao)
    }
    
    @Provides
    @Singleton
    fun provideDailySummaryRepository(
        dailySummaryDao: com.mindguard.data.db.DailySummaryDao
    ): DailySummaryRepository {
        return DailySummaryRepository(dailySummaryDao)
    }
    
    @Provides
    @Singleton
    fun provideFocusSessionRepository(
        focusSessionDao: com.mindguard.data.db.FocusSessionDao
    ): FocusSessionRepository {
        return FocusSessionRepository(focusSessionDao)
    }
    
    @Provides
    @Singleton
    fun provideAppCategoryRepository(
        appCategoryMappingDao: com.mindguard.data.db.AppCategoryMappingDao
    ): AppCategoryRepository {
        return AppCategoryRepository(appCategoryMappingDao)
    }
}
