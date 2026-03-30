package com.mindguard.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.ui.dashboard.viewholders.*
import com.mindguard.ui.dashboard.viewholders.WellnessScoreViewHolder
import com.mindguard.ui.dashboard.viewholders.TimeBreakdownViewHolder
import com.mindguard.ui.dashboard.viewholders.TimelineViewHolder
import com.mindguard.ui.dashboard.viewholders.AppLeaderboardViewHolder
import com.mindguard.ui.dashboard.viewholders.StreakAchievementsViewHolder
import com.mindguard.ui.dashboard.viewholders.WeeklyTrendViewHolder

class DashboardAdapter : ListAdapter<DashboardItem, RecyclerView.ViewHolder>(DiffCallback()) {
    
    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (DashboardItemType.values()[viewType]) {
            DashboardItemType.WELLNESS_SCORE -> {
                WellnessScoreViewHolder.create(inflater, parent)
            }
            DashboardItemType.TIME_BREAKDOWN -> {
                TimeBreakdownViewHolder.create(inflater, parent)
            }
            DashboardItemType.TIMELINE -> {
                TimelineViewHolder.create(inflater, parent)
            }
            DashboardItemType.APP_LEADERBOARD -> {
                AppLeaderboardViewHolder.create(inflater, parent)
            }
            DashboardItemType.STREAK_ACHIEVEMENTS -> {
                StreakAchievementsViewHolder.create(inflater, parent)
            }
            DashboardItemType.WEEKLY_TREND -> {
                WeeklyTrendViewHolder.create(inflater, parent)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        
        when (holder) {
            is WellnessScoreViewHolder -> {
                holder.bind(item.data as WellnessScoreData)
            }
            is TimeBreakdownViewHolder -> {
                holder.bind(item.data as TimeBreakdownData)
            }
            is TimelineViewHolder -> {
                holder.bind(item.data as TimelineData)
            }
            is AppLeaderboardViewHolder -> {
                holder.bind(item.data as List<@JvmSuppressWildcards AppUsageData>)
            }
            is StreakAchievementsViewHolder -> {
                holder.bind(item.data as StreakAchievementsData)
            }
            is WeeklyTrendViewHolder -> {
                holder.bind(item.data as WeeklyTrendData)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<DashboardItem>() {
        override fun areItemsTheSame(oldItem: DashboardItem, newItem: DashboardItem): Boolean {
            return oldItem.type == newItem.type
        }
        
        override fun areContentsTheSame(oldItem: DashboardItem, newItem: DashboardItem): Boolean {
            return oldItem == newItem
        }
    }
}
