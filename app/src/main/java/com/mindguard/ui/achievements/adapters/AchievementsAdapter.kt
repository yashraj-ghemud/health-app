package com.mindguard.ui.achievements.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R
import com.mindguard.data.model.Achievement
import com.mindguard.data.model.AchievementCategory

class AchievementsAdapter : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {
    
    private var achievements = listOf<Achievement>()
    private var onAchievementClick: ((Achievement) -> Unit)? = null
    
    fun submitList(newAchievements: List<Achievement>) {
        val diffResult = DiffUtil.calculateDiff(AchievementDiffCallback(achievements, newAchievements))
        achievements = newAchievements
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun setOnAchievementClick(listener: (Achievement) -> Unit) {
        onAchievementClick = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(achievements[position])
    }
    
    override fun getItemCount(): Int = achievements.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val achievementIcon: ImageView = itemView.findViewById(R.id.achievementIcon)
        private val achievementTitle: TextView = itemView.findViewById(R.id.achievementTitle)
        private val achievementDescription: TextView = itemView.findViewById(R.id.achievementDescription)
        private val achievementDate: TextView = itemView.findViewById(R.id.achievementDate)
        private val lockOverlay: View = itemView.findViewById(R.id.lockOverlay)
        private val newBadge: View = itemView.findViewById(R.id.newBadge)
        private val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)
        
        fun bind(achievement: Achievement) {
            achievementTitle.text = achievement.title
            achievementDescription.text = achievement.description
            
            // Set icon based on category and unlock status
            if (achievement.unlockedAt != null) {
                achievementIcon.setImageResource(getIconForCategory(achievement.category))
                achievementIcon.alpha = 1.0f
                lockOverlay.visibility = View.GONE
                
                // Show unlock date
                val date = android.text.format.DateUtils.getRelativeTimeSpanString(
                    achievement.unlockedAt,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.DAY_IN_MILLIS
                )
                achievementDate.text = "Unlocked $date"
                achievementDate.visibility = View.VISIBLE
                
                // Show new badge if recently unlocked
                newBadge.visibility = if (achievement.isNew) View.VISIBLE else View.GONE
                
            } else {
                achievementIcon.setImageResource(R.drawable.ic_locked_achievement)
                achievementIcon.alpha = 0.5f
                lockOverlay.visibility = View.VISIBLE
                achievementDate.visibility = View.GONE
                newBadge.visibility = View.GONE
            }
            
            // Set category indicator color
            categoryIndicator.setBackgroundColor(getColorForCategory(achievement.category))
            
            // Set click listener
            itemView.setOnClickListener {
                onAchievementClick?.invoke(achievement)
            }
        }
        
        private fun getIconForCategory(category: AchievementCategory): Int {
            return when (category) {
                AchievementCategory.FOCUS -> R.drawable.ic_badge_focus
                AchievementCategory.STREAK -> R.drawable.ic_badge_streak
                AchievementCategory.PRODUCTIVITY -> R.drawable.ic_badge_productivity
                AchievementCategory.MILESTONE -> R.drawable.ic_badge_milestone
            }
        }
        
        private fun getColorForCategory(category: AchievementCategory): Int {
            return when (category) {
                AchievementCategory.FOCUS -> itemView.context.getColor(R.color.purple)
                AchievementCategory.STREAK -> itemView.context.getColor(R.color.orange)
                AchievementCategory.PRODUCTIVITY -> itemView.context.getColor(R.color.green)
                AchievementCategory.MILESTONE -> itemView.context.getColor(R.color.blue)
            }
        }
    }
    
    class AchievementDiffCallback(
        private val oldList: List<Achievement>,
        private val newList: List<Achievement>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].achievementKey == newList[newItemPosition].achievementKey
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
