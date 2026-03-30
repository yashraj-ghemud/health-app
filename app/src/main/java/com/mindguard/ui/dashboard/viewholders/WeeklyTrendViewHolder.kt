package com.mindguard.ui.dashboard.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.mindguard.R
import java.text.SimpleDateFormat
import java.util.*

class WeeklyTrendViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val weeklyTrendChart: BarChart = itemView.findViewById(R.id.weeklyTrendChart)
    private val averageProductiveText: TextView = itemView.findViewById(R.id.averageProductiveText)
    private val averageEntertainmentText: TextView = itemView.findViewById(R.id.averageEntertainmentText)
    private val averageScoreText: TextView = itemView.findViewById(R.id.averageScoreText)
    
    fun bind(data: WeeklyTrendData) {
        averageProductiveText.text = formatTime(data.averageProductive)
        averageEntertainmentText.text = formatTime(data.averageEntertainment)
        averageScoreText.text = "${data.averageWellnessScore}/100"
        
        setupWeeklyTrendChart(data.dailyData)
    }
    
    private fun setupWeeklyTrendChart(dailyData: List<DailyTrendData>) {
        val productiveEntries = mutableListOf<BarEntry>()
        val entertainmentEntries = mutableListOf<BarEntry>()
        
        dailyData.forEachIndexed { index, data ->
            productiveEntries.add(BarEntry(index.toFloat(), data.productiveMinutes.toFloat()))
            entertainmentEntries.add(BarEntry(index.toFloat(), data.entertainmentMinutes.toFloat()))
        }
        
        val productiveDataSet = BarDataSet(productiveEntries, "Productive").apply {
            color = itemView.context.getColor(R.color.green)
            setDrawValues(false)
        }
        
        val entertainmentDataSet = BarDataSet(entertainmentEntries, "Entertainment").apply {
            color = itemView.context.getColor(R.color.red)
            setDrawValues(false)
        }
        
        val barData = BarData(productiveDataSet, entertainmentDataSet)
        weeklyTrendChart.data = barData
        
        // Customize chart appearance
        weeklyTrendChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = itemView.context.getColor(R.color.text_primary)
            
            xAxis.apply {
                setDrawGridLines(false)
                textColor = itemView.context.getColor(R.color.text_secondary)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index < dailyData.size) {
                            getDayLabel(dailyData[index].date)
                        } else {
                            ""
                        }
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = itemView.context.getColor(R.color.gray_light)
                textColor = itemView.context.getColor(R.color.text_secondary)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) {
                            "${value.toInt()}m"
                        } else {
                            ""
                        }
                    }
                }
            }
            
            axisRight.isEnabled = false
            
            // Group bars
            barData.groupBars(0f, 0.3f, 0.1f)
        }
        
        weeklyTrendChart.invalidate()
    }
    
    private fun getDayLabel(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
        
        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
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
        fun create(inflater: LayoutInflater, parent: ViewGroup): WeeklyTrendViewHolder {
            val view = inflater.inflate(R.layout.item_weekly_trend, parent, false)
            return WeeklyTrendViewHolder(view)
        }
    }
}
