package com.mindguard.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MissionPagerAdapter(private val context: Context) : RecyclerView.Adapter<MissionPagerAdapter.MissionViewHolder>() {
    
    private val missionItems = listOf(
        MissionItem(
            title = "Smart Monitoring",
            description = "MindGuard runs silently in the background, tracking which apps you use and for how long. No data leaves your device.",
            iconRes = android.R.drawable.ic_menu_recent_history
        ),
        MissionItem(
            title = "Gentle Interventions",
            description = "When you spend too much time on distracting apps, MindGuard shows beautiful overlays with motivational quotes and breathing exercises.",
            iconRes = android.R.drawable.ic_dialog_alert
        ),
        MissionItem(
            title = "Actionable Insights",
            description = "Get daily summaries, wellness scores, and achievement badges that make digital wellness fun and rewarding.",
            iconRes = android.R.drawable.ic_menu_info_details
        )
    )
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return MissionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val item = missionItems[position]
        holder.bind(item)
    }
    
    override fun getItemCount(): Int = missionItems.size
    
    class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(android.R.id.text1)
        private val descriptionText: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(item: MissionItem) {
            titleText.text = item.title
            descriptionText.text = item.description
        }
    }
    
    data class MissionItem(
        val title: String,
        val description: String,
        val iconRes: Int
    )
}
