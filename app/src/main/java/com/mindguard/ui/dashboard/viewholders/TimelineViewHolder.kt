package com.mindguard.ui.dashboard.viewholders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R
import com.mindguard.data.model.AppCategory
import java.text.SimpleDateFormat
import java.util.*

class TimelineViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val timelineView: TimelineView = itemView.findViewById(R.id.timelineView)
    private val dateText: TextView = itemView.findViewById(R.id.dateText)
    
    fun bind(data: TimelineData) {
        dateText.text = formatDate(data.date)
        timelineView.setTimelineBlocks(data.blocks)
    }
    
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        
        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
    
    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): TimelineViewHolder {
            val view = inflater.inflate(R.layout.item_timeline, parent, false)
            return TimelineViewHolder(view)
        }
    }
}

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var timelineBlocks: List<TimelineBlock> = emptyList()
    private val hourHeight = 60f
    private val blockHeight = 40f
    private val timeWidth = 60f
    private val blockMargin = 2f
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        textPaint.apply {
            color = context.getColor(R.color.text_secondary)
            textSize = 12f * resources.displayMetrics.scaledDensity
            textAlign = Paint.Align.RIGHT
        }
        
        backgroundPaint.apply {
            color = context.getColor(R.color.gray_light)
        }
    }
    
    fun setTimelineBlocks(blocks: List<TimelineBlock>) {
        timelineBlocks = blocks
        invalidate()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = (24 * hourHeight).toInt() // 24 hours
        
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (timelineBlocks.isEmpty()) return
        
        drawTimeLabels(canvas)
        drawTimelineBlocks(canvas)
        drawHourLines(canvas)
    }
    
    private fun drawTimeLabels(canvas: Canvas) {
        for (hour in 0..23) {
            val y = hour * hourHeight + 20f
            val timeLabel = String.format("%02d:00", hour)
            canvas.drawText(timeLabel, timeWidth - 10f, y, textPaint)
        }
    }
    
    private fun drawHourLines(canvas: Canvas) {
        for (hour in 0..23) {
            val y = hour * hourHeight
            canvas.drawLine(timeWidth, y, width.toFloat(), y, backgroundPaint)
        }
    }
    
    private fun drawTimelineBlocks(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        
        timelineBlocks.forEach { block ->
            calendar.timeInMillis = block.startTime
            val startHour = calendar.get(Calendar.HOUR_OF_DAY)
            val startMinute = calendar.get(Calendar.MINUTE)
            val startY = (startHour * hourHeight) + (startMinute * hourHeight / 60f)
            
            calendar.timeInMillis = block.endTime
            val endHour = calendar.get(Calendar.HOUR_OF_DAY)
            val endMinute = calendar.get(Calendar.MINUTE)
            val endY = (endHour * hourHeight) + (endMinute * hourHeight / 60f)
            
            val blockLeft = timeWidth + blockMargin
            val blockRight = width.toFloat() - blockMargin
            val blockTop = startY
            val blockBottom = endY
            
            // Draw block background
            paint.color = getColorForCategory(block.category)
            canvas.drawRect(blockLeft, blockTop, blockRight, blockBottom, paint)
            
            // Draw app label if block is tall enough
            if (blockBottom - blockTop > 30f) {
                textPaint.color = context.getColor(R.color.white)
                textPaint.textAlign = Paint.Align.LEFT
                
                val textBounds = Rect()
                textPaint.getTextBounds(block.appLabel, 0, block.appLabel.length, textBounds)
                
                val textX = blockLeft + 10f
                val textY = blockTop + (blockBottom - blockTop) / 2f + textBounds.height() / 2f
                
                // Truncate text if necessary
                val maxTextWidth = blockRight - blockLeft - 20f
                val displayText = if (textBounds.width() > maxTextWidth) {
                    val ellipsis = "..."
                    val truncatedText = android.text.TextUtils.ellipsize(block.appLabel, textPaint as android.text.TextPaint, maxTextWidth, 
                        android.text.TextUtils.TruncateAt.END)
                    truncatedText?.toString() ?: block.appLabel
                } else {
                    block.appLabel
                }
                
                canvas.drawText(displayText, textX, textY, textPaint)
                textPaint.color = context.getColor(R.color.text_secondary)
                textPaint.textAlign = Paint.Align.RIGHT
            }
        }
    }
    
    private fun getColorForCategory(category: AppCategory): Int {
        return when (category) {
            AppCategory.DEEP_WORK -> context.getColor(R.color.green)
            AppCategory.COMMUNICATION -> context.getColor(R.color.blue)
            AppCategory.PASSIVE_ENTERTAINMENT -> context.getColor(R.color.red)
            AppCategory.PASSIVE_SCROLL -> context.getColor(R.color.orange)
            AppCategory.SYSTEM_UTILITY -> context.getColor(R.color.gray)
            AppCategory.NEUTRAL -> context.getColor(R.color.blue_gray)
        }
    }
}
