package com.mindguard.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.room.ProvidedTypeConverter
import com.mindguard.data.model.*

@ProvidedTypeConverter
class Converters {
    
    @TypeConverter
    fun fromAppCategory(category: AppCategory): String = category.name
    
    @TypeConverter
    fun toAppCategory(category: String): AppCategory = AppCategory.valueOf(category)
    
    @TypeConverter
    fun fromSessionType(sessionType: SessionType): String = sessionType.name
    
    @TypeConverter
    fun toSessionType(sessionType: String): SessionType = SessionType.valueOf(sessionType)
    
    @TypeConverter
    fun fromTriggerType(triggerType: TriggerType): String = triggerType.name
    
    @TypeConverter
    fun toTriggerType(triggerType: String): TriggerType = TriggerType.valueOf(triggerType)
    
    @TypeConverter
    fun fromActionType(actionType: ActionType): String = actionType.name
    
    @TypeConverter
    fun toActionType(actionType: String): ActionType = ActionType.valueOf(actionType)
    
    @TypeConverter
    fun fromStrictLevel(strictLevel: StrictLevel): String = strictLevel.name
    
    @TypeConverter
    fun toStrictLevel(strictLevel: String): StrictLevel = StrictLevel.valueOf(strictLevel)
    
    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory): String = category.name
    
    @TypeConverter
    fun toAchievementCategory(category: String): AchievementCategory = AchievementCategory.valueOf(category)
    
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    
    @TypeConverter
    fun fromAppCategoryList(list: List<AppCategory>): String {
        val gson = Gson()
        return gson.toJson(list.map { it.name })
    }
    
    @TypeConverter
    fun toAppCategoryList(value: String): List<AppCategory> {
        val gson = Gson()
        val stringList = gson.fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type)
        return stringList?.map { AppCategory.valueOf(it) } ?: emptyList()
    }
}
