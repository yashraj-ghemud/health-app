package com.mindguard.intervention

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.mindguard.R
import com.mindguard.data.model.UsageRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InterventionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    
    private var appIcon: ImageView
    private var appName: TextView
    private var messageText: TextView
    private var quoteText: TextView
    private var countdownText: TextView
    private var progressBar: View
    private var breathingCircle: View
    private var bypassButton: View
    private var closeOverlayButton: View
    
    private var countDownTimer: CountDownTimer? = null
    private var breathingAnimator: ValueAnimator? = null
    private var currentRule: UsageRule? = null
    private var currentPackageName: String = ""
    
    private val _isShowing = MutableStateFlow(false)
    val isShowing: StateFlow<Boolean> = _isShowing
    
    private var onDismissListener: (() -> Unit)? = null
    private var onBypassListener: ((String) -> Unit)? = null
    
    init {
        LayoutInflater.from(context).inflate(R.layout.overlay_intervention, this, true)
        
        appIcon = findViewById(R.id.appIcon)
        appName = findViewById(R.id.appName)
        messageText = findViewById(R.id.messageText)
        quoteText = findViewById(R.id.quoteText)
        countdownText = findViewById(R.id.countdownText)
        progressBar = findViewById(R.id.progressBar)
        breathingCircle = findViewById(R.id.breathingCircle)
        bypassButton = findViewById(R.id.bypassButton)
        closeOverlayButton = findViewById(R.id.closeOverlayButton)
        
        setupClickListeners()
        alpha = 0f
        visibility = View.GONE
    }
    
    private fun setupClickListeners() {
        bypassButton.setOnClickListener {
            showBypassDialog()
        }
        
        closeOverlayButton.setOnClickListener {
            // Only allow closing if timer has finished
            if (countDownTimer == null) {
                dismiss()
            }
        }
    }
    
    fun showIntervention(
        packageName: String,
        appLabel: String,
        rule: UsageRule,
        durationMinutes: Int,
        quote: String? = null
    ) {
        currentPackageName = packageName
        currentRule = rule
        
        // Set app info
        try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
        } catch (e: PackageManager.NameNotFoundException) {
            appIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_app_default))
        }
        
        appName.text = appLabel
        
        // Set message based on rule and duration
        messageText.text = when (rule.actionType) {
            com.mindguard.data.model.ActionType.BLOCK -> {
                "You've been on $appLabel for too long. Take a ${durationMinutes}-minute break."
            }
            com.mindguard.data.model.ActionType.NOTIFY -> {
                "Time for a break from $appLabel?"
            }
            com.mindguard.data.model.ActionType.OVERLAY -> {
                "Consider taking a break from $appLabel"
            }
        }
        
        // Set quote
        quoteText.text = quote ?: getRandomMotivationalQuote()
        
        // Show appropriate UI based on duration
        if (durationMinutes >= 5) {
            // Show breathing exercise for longer blocks
            showBreathingExercise(durationMinutes)
        } else {
            // Show simple countdown for shorter blocks
            showCountdown(durationMinutes)
        }
        
        // Show bypass button for gentle mode
        bypassButton.visibility = if (rule.strictLevel == com.mindguard.data.model.StrictLevel.GENTLE) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        // Animate in
        animateIn()
    }
    
    private fun showCountdown(durationMinutes: Int) {
        breathingCircle.visibility = View.GONE
        countdownText.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        
        val totalMillis = durationMinutes * 60 * 1000L
        
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutesRemaining = millisUntilFinished / 60000
                val secondsRemaining = (millisUntilFinished % 60000) / 1000
                
                countdownText.text = String.format("%02d:%02d", minutesRemaining, secondsRemaining)
                
                // Update progress bar
                val progress = ((totalMillis - millisUntilFinished).toFloat() / totalMillis) * 100
                progressBar.scaleX = progress / 100f
            }
            
            override fun onFinish() {
                countdownText.text = "00:00"
                progressBar.scaleX = 1f
                onCountdownFinished()
            }
        }.start()
    }
    
    private fun showBreathingExercise(durationMinutes: Int) {
        breathingCircle.visibility = View.VISIBLE
        countdownText.visibility = View.GONE
        progressBar.visibility = View.GONE
        
        val totalMillis = durationMinutes * 60 * 1000L
        var cycleCount = 0
        val totalCycles = durationMinutes * 2 // 2 cycles per minute
        
        countDownTimer = object : CountDownTimer(totalMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val cycleProgress = ((totalMillis - millisUntilFinished) % 4000) / 4000f // 4-second cycle
                
                if (cycleProgress < 0.5f) {
                    // Inhale (0-2 seconds)
                    val scale = 1f + (cycleProgress * 2f) // Scale from 1x to 2x
                    breathingCircle.scaleX = scale
                    breathingCircle.scaleY = scale
                    quoteText.text = "Breathe In..."
                } else {
                    // Exhale (2-4 seconds)
                    val scale = 2f - ((cycleProgress - 0.5f) * 2f) // Scale from 2x to 1x
                    breathingCircle.scaleX = scale
                    breathingCircle.scaleY = scale
                    quoteText.text = "Breathe Out..."
                }
                
                // Update cycle count
                val newCycleCount = ((totalMillis - millisUntilFinished) / 4000).toInt()
                if (newCycleCount != cycleCount) {
                    cycleCount = newCycleCount
                    if (cycleCount < totalCycles) {
                        countdownText.text = "${totalCycles - cycleCount} cycles remaining"
                    }
                }
            }
            
            override fun onFinish() {
                breathingCircle.scaleX = 1f
                breathingCircle.scaleY = 1f
                countdownText.text = "Complete!"
                onCountdownFinished()
            }
        }.start()
    }
    
    private fun onCountdownFinished() {
        countDownTimer = null
        closeOverlayButton.visibility = View.VISIBLE
        messageText.text = "Break complete! You can now return to your app."
        
        // Auto-dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
        }, 3000)
    }
    
    private fun showBypassDialog() {
        // This would show a dialog asking for reason
        // For now, just call the bypass listener
        onBypassListener?.invoke(currentPackageName)
    }
    
    private fun animateIn() {
        visibility = View.VISIBLE
        
        val fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        fadeIn.duration = 300
        fadeIn.interpolator = AccelerateDecelerateInterpolator()
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                _isShowing.value = true
            }
        })
        fadeIn.start()
    }
    
    fun dismiss() {
        countDownTimer?.cancel()
        breathingAnimator?.cancel()
        
        val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        fadeOut.duration = 300
        fadeOut.interpolator = AccelerateDecelerateInterpolator()
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                _isShowing.value = false
                onDismissListener?.invoke()
            }
        })
        fadeOut.start()
    }
    
    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }
    
    fun setOnBypassListener(listener: (String) -> Unit) {
        onBypassListener = listener
    }
    
    private fun getRandomMotivationalQuote(): String {
        val quotes = listOf(
            "Your time is limited, don't waste it living someone else's life.",
            "The only way to do great work is to love what you do.",
            "Focus on being productive instead of busy.",
            "You don't have to be great to start, but you have to start to be great.",
            "The secret of getting ahead is getting started.",
            "Don't watch the clock; do what it does. Keep going.",
            "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            "Believe you can and you're halfway there.",
            "The future depends on what you do today.",
            "Quality is not an act, it is a habit.",
            "Your limitation—it's only your imagination.",
            "Great things never come from comfort zones.",
            "Dream it. Wish it. Do it.",
            "Success doesn't just find you. You have to go out and get it.",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Don't stop when you're tired. Stop when you're done.",
            "Wake up with determination. Go to bed with satisfaction.",
            "Do something today that your future self will thank you for.",
            "Little things make big days.",
            "It's going to be hard, but hard does not mean impossible."
        )
        
        return quotes.random()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer?.cancel()
        breathingAnimator?.cancel()
    }
}
