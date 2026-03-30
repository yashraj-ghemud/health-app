package com.mindguard.ui.focus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mindguard.databinding.ActivitySessionCompleteBinding
import com.mindguard.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SessionCompleteActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySessionCompleteBinding
    private val viewModel: SessionCompleteViewModel by viewModels()
    
    @Inject
    lateinit var achievementEngine: com.mindguard.domain.engine.AchievementEngine
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadSessionData()
        checkAchievements()
    }
    
    private fun setupUI() {
        binding.btnStartNewSession.setOnClickListener {
            startNewSession()
        }
        
        binding.btnViewDashboard.setOnClickListener {
            viewDashboard()
        }
        
        binding.btnClose.setOnClickListener {
            closeActivity()
        }
    }
    
    private fun loadSessionData() {
        val sessionDuration = intent.getIntExtra("session_duration", 0)
        val sessionGoal = intent.getStringExtra("session_goal") ?: "Focus Session"
        
        binding.tvGoal.text = sessionGoal
        binding.tvDuration.text = formatDuration(sessionDuration)
        binding.tvMessage.text = getCompletionMessage(sessionDuration)
        
        // Show confetti for successful completion
        if (sessionDuration > 0) {
            showConfetti()
        }
    }
    
    private fun checkAchievements() {
        val sessionDuration = intent.getIntExtra("session_duration", 0)
        
        // Check for achievements
        lifecycleScope.launch {
            achievementEngine.checkFocusSessionAchievements(sessionDuration) { unlockedAchievements ->
                if (unlockedAchievements.isNotEmpty()) {
                    showAchievementUnlocked(unlockedAchievements)
                }
            }
        }
    }
    
    private fun showConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xb48def, 0xf4306d),
            emitter = Emitter(duration = 1, TimeUnit.SECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        
        binding.konfettiView.start(party)
    }
    
    private fun showAchievementUnlocked(achievements: List<com.mindguard.data.model.Achievement>) {
        val achievementNames = achievements.joinToString("\n") { "🏆 ${it.title}" }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Achievements Unlocked!")
            .setMessage(achievementNames)
            .setPositiveButton("Awesome!") { _, _ ->
                // Dismiss dialog
            }
            .setCancelable(false)
            .show()
    }
    
    private fun getCompletionMessage(durationMinutes: Int): String {
        return when {
            durationMinutes >= 120 -> "Incredible! 2+ hours of deep focus!"
            durationMinutes >= 60 -> "Excellent work! One hour of focused time."
            durationMinutes >= 45 -> "Great job! Almost an hour of productivity."
            durationMinutes >= 25 -> "Well done! You completed a Pomodoro session."
            durationMinutes >= 15 -> "Good start! Every minute counts."
            else -> "Thanks for trying! Next time will be even better."
        }
    }
    
    private fun formatDuration(minutes: Int): String {
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
    
    private fun startNewSession() {
        val intent = Intent(this, FocusSessionActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun viewDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("open_tab", "dashboard")
        startActivity(intent)
        finish()
    }
    
    private fun closeActivity() {
        finish()
    }
    
    override fun onBackPressed() {
        // Handle back press - go to dashboard
        viewDashboard()
    }
}
