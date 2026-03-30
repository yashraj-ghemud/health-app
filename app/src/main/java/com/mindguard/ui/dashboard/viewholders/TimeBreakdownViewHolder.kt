package com.mindguard.ui.dashboard.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.mindguard.R
import com.mindguard.data.model.AppCategory
import java.text.SimpleDateFormat
import java.util.*

class TimeBreakdownViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val timeBreakdownChart: PieChart = itemView.findViewById(R.id.timeBreakdownChart)
    private val totalTimeText: TextView = itemView.findViewById(R.id.totalTimeText)
    private val categoryLegend: ViewGroup = itemView.findViewById(R.id.categoryLegend)
    
    fun bind(data: TimeBreakdownData) {
        totalTimeText.text = formatTotalTime(data.totalMinutes)
        setupTimeBreakdownChart(data.categories)
        setupCategoryLegend(data.categories)
    }
    
    private fun setupTimeBreakdownChart(categories: Map<AppCategory, Int>) {
        val entries = categories.map { (category, minutes) ->
            PieEntry(minutes.toFloat(), category.displayName)
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = categories.keys.map { category ->
                getColorForCategory(category)
            }
            setDrawValues(true)
            setValueTextSize(12f)
            setValueTextColor(itemView.context.getColor(R.color.white))
        }
        
        val pieData = PieData(dataSet)
        timeBreakdownChart.data = pieData
        
        // Customize the chart appearance
        timeBreakdownChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            setHoleColor(itemView.context.getColor(R.color.white))
            setTransparentCircleColor(itemView.context.getColor(R.color.gray_light))
            setTransparentCircleAlpha(50)
            setUsePercentValues(true)
            setEntryLabelColor(itemView.context.getColor(R.color.white))
        }
        
        timeBreakdownChart.invalidate()
    }
    
    private fun setupCategoryLegend(categories: Map<AppCategory, Int>) {
        categoryLegend.removeAllViews()
        
        categories.forEach { (category, minutes) ->
            val legendItem = createLegendItem(category, minutes)
            categoryLegend.addView(legendItem)
        }
    }
    
    private fun createLegendItem(category: AppCategory, minutes: Int): View {
        val legendView = LayoutInflater.from(itemView.context)
            .inflate(R.layout.item_category_legend, categoryLegend, false)
        
        val colorIndicator = legendView.findViewById<View>(R.id.colorIndicator)
        val categoryName = legendView.findViewById<TextView>(R.id.categoryName)
        val categoryTime = legendView.findViewById<TextView>(R.id.categoryTime)
        
        colorIndicator.setBackgroundColor(getColorForCategory(category))
        categoryName.text = category.displayName
        categoryTime.text = formatTime(minutes)
        
        return legendView
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
    
    private fun formatTotalTime(minutes: Int): String {
        return when {
            minutes >= 60 -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "${hours}h ${remainingMinutes}m total"
                } else {
                    "${hours}h total"
                }
            }
            else -> "${minutes}m total"
        }
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
    
    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): TimeBreakdownViewHolder {
            val view = inflater.inflate(R.layout.item_time_breakdown, parent, false)
            return TimeBreakdownViewHolder(view)
        }
    }
}
