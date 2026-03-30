package com.mindguard.data.db

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindguard.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [
        UsageEvent::class,
        UsageRule::class,
        InterventionLog::class,
        Achievement::class,
        DailySummary::class,
        FocusSession::class,
        AppCategoryMapping::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun usageEventDao(): UsageEventDao
    abstract fun usageRuleDao(): UsageRuleDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun appCategoryMappingDao(): AppCategoryMappingDao
    
    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            val dbInstance = database.get()
            applicationScope.launch {
                populateDatabase(dbInstance)
            }
        }
        
        private suspend fun populateDatabase(database: AppDatabase) {
            // Seed default rules
            seedDefaultRules(database.usageRuleDao())
            
            // Seed achievements
            seedAchievements(database.achievementDao())
            
            // Seed app category mappings
            seedAppCategories(database.appCategoryMappingDao())
        }
        
        private suspend fun seedDefaultRules(dao: UsageRuleDao) {
            val defaultRules = listOf(
                UsageRule(
                    id = "entertainment_30_continuous",
                    targetCategory = AppCategory.PASSIVE_ENTERTAINMENT,
                    targetPackage = null,
                    triggerType = TriggerType.CONTINUOUS,
                    thresholdMinutes = 30,
                    actionType = ActionType.BLOCK,
                    blockDurationMinutes = 2,
                    isEnabled = true,
                    strictLevel = StrictLevel.BALANCED
                ),
                UsageRule(
                    id = "entertainment_60_daily",
                    targetCategory = AppCategory.PASSIVE_ENTERTAINMENT,
                    targetPackage = null,
                    triggerType = TriggerType.CUMULATIVE_DAILY,
                    thresholdMinutes = 60,
                    actionType = ActionType.BLOCK,
                    blockDurationMinutes = 10,
                    isEnabled = true,
                    strictLevel = StrictLevel.BALANCED
                ),
                UsageRule(
                    id = "social_45_continuous",
                    targetCategory = AppCategory.PASSIVE_SCROLL,
                    targetPackage = null,
                    triggerType = TriggerType.CONTINUOUS,
                    thresholdMinutes = 45,
                    actionType = ActionType.NOTIFY,
                    blockDurationMinutes = 0,
                    isEnabled = true,
                    strictLevel = StrictLevel.BALANCED
                ),
                UsageRule(
                    id = "youtube_60_continuous",
                    targetPackage = "com.google.android.youtube",
                    targetCategory = null,
                    triggerType = TriggerType.CONTINUOUS,
                    thresholdMinutes = 60,
                    actionType = ActionType.BLOCK,
                    blockDurationMinutes = 5,
                    isEnabled = true,
                    strictLevel = StrictLevel.BALANCED
                ),
                UsageRule(
                    id = "whatsapp_90_continuous",
                    targetPackage = "com.whatsapp",
                    targetCategory = null,
                    triggerType = TriggerType.CONTINUOUS,
                    thresholdMinutes = 90,
                    actionType = ActionType.NOTIFY,
                    blockDurationMinutes = 0,
                    isEnabled = true,
                    strictLevel = StrictLevel.GENTLE
                ),
                UsageRule(
                    id = "total_screen_4h",
                    targetCategory = null,
                    targetPackage = null,
                    triggerType = TriggerType.CUMULATIVE_DAILY,
                    thresholdMinutes = 240,
                    actionType = ActionType.NOTIFY,
                    blockDurationMinutes = 0,
                    isEnabled = true,
                    strictLevel = StrictLevel.BALANCED
                ),
                UsageRule(
                    id = "total_screen_6h",
                    targetCategory = null,
                    targetPackage = null,
                    triggerType = TriggerType.CUMULATIVE_DAILY,
                    thresholdMinutes = 360,
                    actionType = ActionType.BLOCK,
                    blockDurationMinutes = 10,
                    isEnabled = true,
                    strictLevel = StrictLevel.STRICT
                )
            )
            
            dao.insertRules(defaultRules)
        }
        
        private suspend fun seedAchievements(dao: AchievementDao) {
            val achievements = listOf(
                Achievement(
                    achievementKey = "first_focus_session",
                    title = "First Focus Session",
                    description = "Complete your first focus session",
                    iconRes = "ic_focus",
                    unlockedAt = null,
                    category = AchievementCategory.FOCUS
                ),
                Achievement(
                    achievementKey = "instagram_free_day",
                    title = "Instagram Free Day",
                    description = "Go a full day without using Instagram",
                    iconRes = "ic_instagram_free",
                    unlockedAt = null,
                    category = AchievementCategory.PRODUCTIVITY
                ),
                Achievement(
                    achievementKey = "7_day_streak",
                    title = "7 Day Streak",
                    description = "Stay within your limits for 7 consecutive days",
                    iconRes = "ic_streak_7",
                    unlockedAt = null,
                    category = AchievementCategory.STREAK
                ),
                Achievement(
                    achievementKey = "1000_minutes_saved",
                    title = "1000 Minutes Saved",
                    description = "Save 1000 minutes from entertainment apps",
                    iconRes = "ic_time_saved",
                    unlockedAt = null,
                    category = AchievementCategory.MILESTONE
                ),
                Achievement(
                    achievementKey = "night_owl_slayer",
                    title = "Night Owl Slayer",
                    description = "No phone usage after 11 PM for 5 consecutive days",
                    iconRes = "ic_night_owl",
                    unlockedAt = null,
                    category = AchievementCategory.STREAK
                )
            )
            
            dao.insertAchievements(achievements)
        }
        
        private suspend fun seedAppCategories(dao: AppCategoryMappingDao) {
            val appMappings = listOf(
                // Deep Work Apps
                AppCategoryMapping("com.microsoft.office.word", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.microsoft.office.excel", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.microsoft.office.powerpoint", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.google.android.apps.docs.editors.docs", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.google.android.apps.docs.editors.sheets", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.google.android.apps.docs.editors.slides", AppCategory.DEEP_WORK),
                AppCategoryMapping("org.libreoffice", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.notion.android", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.evernote", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.zoho.office", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.slack", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.microsoft.teams", AppCategory.DEEP_WORK),
                AppCategoryMapping("us.zoom.videomeetings", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.spotify.music", AppCategory.DEEP_WORK), // Can be productive for some
                
                // Communication Apps
                AppCategoryMapping("com.whatsapp", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.telegram.messenger", AppCategory.COMMUNICATION),
                AppCategoryMapping("org.thoughtcrime.securesms", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.discord", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.linkedin.android", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.facebook.orca", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.google.android.gm", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.microsoft.office.outlook", AppCategory.COMMUNICATION),
                AppCategoryMapping("com.yahoo.mobile.client.android.mail", AppCategory.COMMUNICATION),
                
                // Entertainment Apps
                AppCategoryMapping("com.instagram.android", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.google.android.youtube", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.tiktok", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.snapchat.android", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.netflix.mediaclient", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.amazon.avod.thirdparty", AppCategory.PASSIVE_ENTERTAINMENT), // Prime Video
                AppCategoryMapping("com.disney.disneyplus", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.hbo.hbonow", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.hulu.plus", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.mxtech.videoplayer.ad", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.mxtech.videoplayer.pro", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("org.videolan.vlc", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("is.xyz.mpv", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.spotify.music", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.apple.android.music", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.soundcloud.android", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.pandora.android", AppCategory.PASSIVE_ENTERTAINMENT),
                
                // Social Media / Passive Scroll
                AppCategoryMapping("com.twitter.android", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.facebook.katana", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.reddit.frontpage", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.pinterest", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.tumblr", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.medium.reader", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.quora.android", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.linkedin.android", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.beeminder.android", AppCategory.PASSIVE_SCROLL),
                
                // Learning Apps (Deep Work)
                AppCategoryMapping("org.coursera.android", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.duolingo", AppCategory.DEEP_WORK),
                AppCategoryMapping("org.khanacademy.android", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.udemy.android", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.sololearn", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.memrise.android.memrisecompanion", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.brainly", AppCategory.DEEP_WORK),
                AppCategoryMapping("comphotomath", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.socratic.org", AppCategory.DEEP_WORK),
                
                // System/Utility Apps
                AppCategoryMapping("com.android.settings", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.apps.nexuslauncher", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.sec.android.app.launcher", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.huawei.android.launcher", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.xiaomi.mihome", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.apps.maps", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.waze", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.dialer", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.contacts", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.mms", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.camera", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.apps.photos", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.sec.android.app.camera", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.miui.gallery", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.calculator2", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.miui.calculator", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.calendar", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.calendar", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.deskclock", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.deskclock", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.filemanager", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.google.android.apps.nbu.files", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.sec.android.app.myfiles", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.mi.android.globalFileexplorer", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.android.vending", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.amazon.venezia", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.huawei.appmarket", AppCategory.SYSTEM_UTILITY),
                AppCategoryMapping("com.xiaomi.market", AppCategory.SYSTEM_UTILITY),
                
                // Banking/Finance (Deep Work)
                AppCategoryMapping("com.google.android.apps.walletnfcrel", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.paypal.android.p2pmobile", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.banking", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.chase", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.wf.wellsfargo", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.bankofamerica", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.citibank.mobile", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.capitalone", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.etrade.mobilepro", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.robinhood.android", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.coinbase.android", AppCategory.DEEP_WORK),
                AppCategoryMapping("com.binance.dev", AppCategory.DEEP_WORK),
                
                // Shopping (Neutral to Entertainment)
                AppCategoryMapping("com.amazon.mShop.android.shopping", AppCategory.NEUTRAL),
                AppCategoryMapping("com.ebay.mobile", AppCategory.NEUTRAL),
                AppCategoryMapping("com.walmart.android", AppCategory.NEUTRAL),
                AppCategoryMapping("com.target.android", AppCategory.NEUTRAL),
                AppCategoryMapping("com.etsy.android", AppCategory.NEUTRAL),
                AppCategoryMapping("com.shopify.shop", AppCategory.NEUTRAL),
                
                // News (Passive Scroll)
                AppCategoryMapping("com.google.android.apps.magazines", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.nytimes.android", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.cnn.mobile.android", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.bbc.mobile.news.ww", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.reuters", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.theverge", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.techcrunch", AppCategory.PASSIVE_SCROLL),
                AppCategoryMapping("com.inshorts", AppCategory.PASSIVE_SCROLL),
                
                // Gaming (Entertainment)
                AppCategoryMapping("com.king.candycrushsaga", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.roblox.client", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.supercell.clashofclans", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.supercell.clashroyale", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.riotgames.league.wildrift", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.epicgames.fortnite", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.gameloft.android.ANMP.GloftA8HM", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.ea.game.fifa14_row", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.activision.callofduty.shooter", AppCategory.PASSIVE_ENTERTAINMENT),
                AppCategoryMapping("com.mojang.minecraftpe", AppCategory.PASSIVE_ENTERTAINMENT),
                
                // Browsers (Neutral to Passive Scroll)
                AppCategoryMapping("com.android.chrome", AppCategory.NEUTRAL),
                AppCategoryMapping("org.mozilla.firefox", AppCategory.NEUTRAL),
                AppCategoryMapping("com.opera.browser", AppCategory.NEUTRAL),
                AppCategoryMapping("com.brave.browser", AppCategory.NEUTRAL),
                AppCategoryMapping("com.duckduckgo.mobile.android", AppCategory.NEUTRAL),
                AppCategoryMapping("com.microsoft.emmx", AppCategory.NEUTRAL),
                AppCategoryMapping("com.uc.browser.en", AppCategory.NEUTRAL),
                AppCategoryMapping("com.tencent.mtt", AppCategory.NEUTRAL),
                AppCategoryMapping("com.sec.android.app.sbrowser", AppCategory.NEUTRAL),
                AppCategoryMapping("com.mi.globalbrowser", AppCategory.NEUTRAL)
            )
            
            dao.insertMappings(appMappings)
        }
    }
}
