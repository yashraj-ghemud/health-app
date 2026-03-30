package com.mindguard.ui.dashboard.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R
import com.mindguard.data.model.AppCategory

class AppLeaderboardViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewApps)
    private lateinit var appLeaderboardAdapter: AppLeaderboardAdapter
    
    fun bind(apps: List<AppUsageData>) {
        setupRecyclerView(apps)
    }
    
    private fun setupRecyclerView(apps: List<AppUsageData>) {
        appLeaderboardAdapter = AppLeaderboardAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(itemView.context)
            adapter = appLeaderboardAdapter
        }
        
        appLeaderboardAdapter.submitList(apps)
    }
    
    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): AppLeaderboardViewHolder {
            val view = inflater.inflate(R.layout.item_app_leaderboard, parent, false)
            return AppLeaderboardViewHolder(view)
        }
    }
}

class AppLeaderboardAdapter : androidx.recyclerview.widget.ListAdapter<AppUsageData, AppLeaderboardAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_usage, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val appTime: TextView = itemView.findViewById(R.id.appTime)
        private val timeBar: View = itemView.findViewById(R.id.timeBar)
        private val warningIcon: View = itemView.findViewById(R.id.warningIcon)
        private val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)
        
        fun bind(app: AppUsageData) {
            // Set app icon (would need PackageManager to get actual icon)
            // For now, use a placeholder
            appIcon.setImageResource(R.drawable.ic_app_default)
            
            appName.text = app.appLabel
            appTime.text = formatTime(app.totalMinutes)
            
            // Set time bar width based on usage
            val maxTime = 180f // 3 hours as reference
            val barWidth = (app.totalMinutes.toFloat() / maxTime) * itemView.width
            timeBar.layoutParams.width = barWidth.toInt()
            
            // Show warning if over limit
            warningIcon.visibility = if (app.isOverLimit) View.VISIBLE else View.GONE
            
            // Set category indicator color
            categoryIndicator.setBackgroundColor(getColorForCategory(app.category))
        }
        
        private fun formatTime(minutes: Int): String {
            return when {
                minutes >= 60 -> {
                    val hours = minutes / 60
                    val remainingMinutes = minutes % 60
                    if (remainingMinutes > 0) {
                        "${hours}h ${remainingMinutes}m"
                    } else {
                        "${hours}h"
                    }
                }
                else -> "${minutes}m"
            }
        }
        
        private fun getColorForCategory(category: AppCategory): Int {
            return when (category) {
                AppCategory.DEEP_WORK -> itemView.context.getColor(R.color.green)
                AppCategory.COMMUNICATION -> itemView.context.getColor(R.color.blue)
                AppCategory.PASSIVE_ENTERTAINMENT -> itemView.context.getColor(R.color.red)
                AppCategory.PASSIVE_SCROLL -> itemView.context.getColor(R.color.orange)
                AppCategory.SYSTEM_UTILITY -> itemView.context.getColor(R.color.gray)
                AppCategory.NEUTRAL -> itemView.context.getColor(R.color.blue_gray)
            }
        }
    }
    
    class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<AppUsageData>() {
        override fun areItemsTheSame(oldItem: AppUsageData, newItem: AppUsageData): Boolean {
            return oldItem.packageName == newItem.packageName
        }
        
        override fun areContentsTheSame(oldItem: AppUsageData, newItem: AppUsageData): Boolean {
            return oldItem == newItem
        }
    }
}
