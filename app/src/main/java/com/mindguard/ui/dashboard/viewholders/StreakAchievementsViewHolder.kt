package com.mindguard.ui.dashboard.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R

class StreakAchievementsViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val currentStreakText: TextView = itemView.findViewById(R.id.currentStreakText)
    private val bestStreakText: TextView = itemView.findViewById(R.id.bestStreakText)
    private val achievementsProgress: TextView = itemView.findViewById(R.id.achievementsProgress)
    private val recentAchievementsRecycler: RecyclerView = itemView.findViewById(R.id.recentAchievementsRecycler)
    
    private lateinit var recentAchievementsAdapter: RecentAchievementsAdapter
    
    fun bind(data: StreakAchievementsData) {
        currentStreakText.text = "${data.currentStreak} days"
        bestStreakText.text = "Best: ${data.bestStreak} days"
        achievementsProgress.text = "${data.unlockedAchievements}/${data.totalAchievements}"
        
        setupRecentAchievements(data.recentAchievements)
    }
    
    private fun setupRecentAchievements(achievements: List<com.mindguard.data.model.Achievement>) {
        recentAchievementsAdapter = RecentAchievementsAdapter()
        recentAchievementsRecycler.apply {
            layoutManager = GridLayoutManager(itemView.context, 3)
            adapter = recentAchievementsAdapter
        }
        
        recentAchievementsAdapter.submitList(achievements.take(6)) // Show max 6 recent achievements
    }
    
    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): StreakAchievementsViewHolder {
            val view = inflater.inflate(R.layout.item_streak_achievements, parent, false)
            return StreakAchievementsViewHolder(view)
        }
    }
}

class RecentAchievementsAdapter : androidx.recyclerview.widget.ListAdapter<com.mindguard.data.model.Achievement, RecentAchievementsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement_badge, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badgeIcon: View = itemView.findViewById(R.id.badgeIcon)
        private val badgeTitle: TextView = itemView.findViewById(R.id.badgeTitle)
        
        fun bind(achievement: com.mindguard.data.model.Achievement) {
            badgeTitle.text = achievement.title
            
            // Set badge icon based on category
            val iconResource = when (achievement.category) {
                com.mindguard.data.model.AchievementCategory.FOCUS -> R.drawable.ic_badge_focus
                com.mindguard.data.model.AchievementCategory.STREAK -> R.drawable.ic_badge_streak
                com.mindguard.data.model.AchievementCategory.PRODUCTIVITY -> R.drawable.ic_badge_productivity
                com.mindguard.data.model.AchievementCategory.MILESTONE -> R.drawable.ic_badge_milestone
            }
            
            badgeIcon.setBackgroundResource(iconResource)
            
            // Highlight new achievements
            if (achievement.isNew) {
                badgeIcon.alpha = 1.0f
            } else {
                badgeIcon.alpha = 0.7f
            }
        }
    }
    
    class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<com.mindguard.data.model.Achievement>() {
        override fun areItemsTheSame(oldItem: com.mindguard.data.model.Achievement, newItem: com.mindguard.data.model.Achievement): Boolean {
            return oldItem.achievementKey == newItem.achievementKey
        }
        
        override fun areContentsTheSame(oldItem: com.mindguard.data.model.Achievement, newItem: com.mindguard.data.model.Achievement): Boolean {
            return oldItem == newItem
        }
    }
}
