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

class WellnessScoreViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val wellnessScoreText: TextView = itemView.findViewById(R.id.wellnessScoreText)
    private val dateText: TextView = itemView.findViewById(R.id.dateText)
    private val trendText: TextView = itemView.findViewById(R.id.trendText)
    private val scoreRing: PieChart = itemView.findViewById(R.id.scoreRing)
    
    fun bind(data: WellnessScoreData) {
        wellnessScoreText.text = data.score.toString()
        dateText.text = data.date
        trendText.text = data.trend
        
        setupScoreRing(data.score)
    }
    
    private fun setupScoreRing(score: Int) {
        val entries = listOf(
            PieEntry(score.toFloat(), "Score"),
            PieEntry((100 - score).toFloat(), "Remaining")
        )
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                getColorForScore(score),
                itemView.context.getColor(R.color.gray_light)
            )
            setDrawValues(false)
            setDrawIcons(false)
        }
        
        val pieData = PieData(dataSet)
        scoreRing.data = pieData
        
        // Customize the chart appearance
        scoreRing.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 75f
            transparentCircleRadius = 80f
            setHoleColor(itemView.context.getColor(R.color.transparent))
            setTransparentCircleColor(itemView.context.getColor(R.color.white))
            setTransparentCircleAlpha(50)
            setUsePercentValues(false)
            setEntryLabelColor(itemView.context.getColor(R.color.transparent))
        }
        
        scoreRing.invalidate()
    }
    
    private fun getColorForScore(score: Int): Int {
        return when {
            score >= 80 -> itemView.context.getColor(R.color.green)
            score >= 60 -> itemView.context.getColor(R.color.yellow)
            score >= 40 -> itemView.context.getColor(R.color.orange)
            else -> itemView.context.getColor(R.color.red)
        }
    }
    
    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): WellnessScoreViewHolder {
            val view = inflater.inflate(R.layout.item_wellness_score, parent, false)
            return WellnessScoreViewHolder(view)
        }
    }
}
