package com.mindguard.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R

class MissionPagerAdapter(private val context: Context) : RecyclerView.Adapter<MissionPagerAdapter.MissionViewHolder>() {
    
    private val missionItems = listOf(
        MissionItem(
            title = "Smart Monitoring",
            description = "MindGuard runs silently in the background, tracking which apps you use and for how long. No data leaves your device.",
            iconRes = R.drawable.ic_monitor
        ),
        MissionItem(
            title = "Gentle Interventions",
            description = "When you spend too much time on distracting apps, MindGuard shows beautiful overlays with motivational quotes and breathing exercises.",
            iconRes = R.drawable.ic_wellness
        ),
        MissionItem(
            title = "Actionable Insights",
            description = "Get daily summaries, wellness scores, and achievement badges that make digital wellness fun and rewarding.",
            iconRes = R.drawable.ic_focus
        )
    )
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val item = missionItems[position]
        holder.bind(item)
    }
    
    override fun getItemCount(): Int = missionItems.size
    
    class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvMissionTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.tvMissionDescription)
        private val iconView: ImageView = itemView.findViewById(R.id.ivMissionIcon)
        
        fun bind(item: MissionItem) {
            titleText.text = item.title
            descriptionText.text = item.description
            iconView.setImageResource(item.iconRes)
        }
    }
    
    data class MissionItem(
        val title: String,
        val description: String,
        val iconRes: Int
    )
}
