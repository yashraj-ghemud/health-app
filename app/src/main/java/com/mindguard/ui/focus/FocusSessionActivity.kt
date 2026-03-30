package com.mindguard.ui.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.mindguard.R
import com.mindguard.databinding.ActivityFocusSessionBinding
import com.mindguard.service.UsageMonitorService
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FocusSessionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFocusSessionBinding
    private val viewModel: FocusSessionViewModel by viewModels()
    
    @Inject
    lateinit var focusSessionManager: com.mindguard.domain.engine.FocusSessionManager
    
    private var countDownTimer: CountDownTimer? = null
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "focus_session_channel"
        private const val NOTIFICATION_ID = 3001
        
        fun startFocusSession(context: Context) {
            val intent = Intent(context, FocusSessionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        createNotificationChannel()
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.btnStartSession.setOnClickListener {
            startFocusSession()
        }
        
        binding.btnPauseSession.setOnClickListener {
            pauseFocusSession()
        }
        
        binding.btnStopSession.setOnClickListener {
            stopFocusSession()
        }
        
        binding.btn25min.setOnClickListener {
            selectDuration(25)
        }
        
        binding.btn45min.setOnClickListener {
            selectDuration(45)
        }
        
        binding.btn60min.setOnClickListener {
            selectDuration(60)
        }
        
        // binding.btnCustom.setOnClickListener {
        //     showCustomDurationDialog()
        // }
        
        // Set default selection
        selectDuration(25)
    }
    
    private fun observeViewModel() {
        viewModel.sessionState.observe(this) { state ->
            updateUIForSessionState(state)
        }
        
        viewModel.remainingTime.observe(this) { time ->
            updateTimerDisplay(time)
        }
        
        viewModel.sessionProgress.observe(this) { progress ->
            // Update progress if needed
        }
        
        viewModel.currentSession.observe(this) { session ->
            session?.let {
                // Update goal if needed
            }
        }
    }
    
    private fun selectDuration(minutes: Int) {
        // Reset button states
        binding.btn25min.isSelected = false
        binding.btn45min.isSelected = false
        binding.btn60min.isSelected = false
        
        // Set selected button
        when (minutes) {
            25 -> binding.btn25min.isSelected = true
            45 -> binding.btn45min.isSelected = true
            60 -> binding.btn60min.isSelected = true
        }
        
        viewModel.setSessionDuration(minutes)
        // binding.tvDuration.text = "$minutes minutes"
    }
    
    private fun showCustomDurationDialog() {
        // Create custom duration dialog
        // val dialog = com.mindguard.ui.common.CustomDurationDialog { minutes ->
        //     viewModel.setSessionDuration(minutes)
        //     binding.tvDuration.text = "$minutes minutes"
        //     
        //     // Deselect preset buttons
        //     binding.btn25min.isSelected = false
        //     binding.btn45min.isSelected = false
        //     binding.btn60min.isSelected = false
        // }
        // dialog.show(supportFragmentManager, "CustomDurationDialog")
    }
    
    private fun startFocusSession() {
        val goal = "Focus Session"
        val allowedCategories = getAllowedCategories()
        
        viewModel.startFocusSession(goal, allowedCategories) { success ->
            if (success) {
                showSessionNotification()
                updateUIForSessionState(com.mindguard.ui.focus.FocusSessionState.ACTIVE)
            } else {
                Toast.makeText(this, "Failed to start focus session", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun pauseFocusSession() {
        viewModel.pauseFocusSession()
        updateUIForSessionState(com.mindguard.ui.focus.FocusSessionState.PAUSED)
    }
    
    private fun stopFocusSession() {
        viewModel.stopFocusSession { success ->
            if (success) {
                hideSessionNotification()
                showCompletionScreen()
            } else {
                Toast.makeText(this, "Failed to stop focus session", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun getAllowedCategories(): Set<com.mindguard.data.model.AppCategory> {
        val allowedCategories = mutableSetOf<com.mindguard.data.model.AppCategory>()
        
        if (binding.chkDeepWork.isChecked) {
            allowedCategories.add(com.mindguard.data.model.AppCategory.DEEP_WORK)
        }
        if (binding.chkCommunication.isChecked) {
            allowedCategories.add(com.mindguard.data.model.AppCategory.COMMUNICATION)
        }
        
        return allowedCategories
    }
    
    private fun updateUIForSessionState(state: com.mindguard.ui.focus.FocusSessionState) {
        when (state) {
            com.mindguard.ui.focus.FocusSessionState.SETUP -> {
                binding.layoutSetup.visibility = View.VISIBLE
                binding.layoutActive.visibility = View.GONE
                binding.btnStartSession.visibility = View.VISIBLE
                binding.btnPauseSession.visibility = View.GONE
                binding.btnStopSession.visibility = View.GONE
            }
            com.mindguard.ui.focus.FocusSessionState.ACTIVE -> {
                binding.layoutSetup.visibility = View.GONE
                binding.layoutActive.visibility = View.VISIBLE
                binding.btnStartSession.visibility = View.GONE
                binding.btnPauseSession.visibility = View.VISIBLE
                binding.btnStopSession.visibility = View.VISIBLE
            }
            com.mindguard.ui.focus.FocusSessionState.PAUSED -> {
                binding.layoutSetup.visibility = View.GONE
                binding.layoutActive.visibility = View.VISIBLE
                binding.btnStartSession.visibility = View.VISIBLE
                binding.btnStartSession.text = "Resume"
                binding.btnPauseSession.visibility = View.GONE
                binding.btnStopSession.visibility = View.VISIBLE
            }
            com.mindguard.ui.focus.FocusSessionState.COMPLETED -> {
                binding.layoutSetup.visibility = View.VISIBLE
                binding.layoutActive.visibility = View.GONE
                binding.btnStartSession.visibility = View.VISIBLE
                binding.btnStartSession.text = "Start New Session"
                binding.btnPauseSession.visibility = View.GONE
                binding.btnStopSession.visibility = View.GONE
            }
        }
    }
    
    private fun updateTimerDisplay(remainingMinutes: Int) {
        val hours = remainingMinutes / 60
        val minutes = remainingMinutes % 60
        
        binding.tvTimer.text = if (hours > 0) {
            String.format("%02d:%02d:00", hours, minutes)
        } else {
            String.format("%02d:00", minutes)
        }
    }
    
    private fun showSessionNotification() {
        val intent = Intent(this, FocusSessionActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus Session Active")
            .setContentText("Stay focused! ${viewModel.remainingTime.value} minutes remaining")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun hideSessionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    private fun showCompletionScreen() {
        val intent = Intent(this, SessionCompleteActivity::class.java)
        intent.putExtra("session_duration", viewModel.getCompletedSessionDuration())
        intent.putExtra("session_goal", viewModel.getCurrentSessionGoal())
        startActivity(intent)
        finish()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for active focus sessions"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        hideSessionNotification()
    }
    
    override fun onBackPressed() {
        if (viewModel.sessionState.value == com.mindguard.ui.focus.FocusSessionState.ACTIVE) {
            // Show confirmation dialog before closing active session
            showCloseConfirmationDialog()
        } else {
            super.onBackPressed()
        }
    }
    
    private fun showCloseConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("End Focus Session?")
            .setMessage("You have an active focus session. Are you sure you want to end it?")
            .setPositiveButton("End Session") { _, _ ->
                stopFocusSession()
            }
            .setNegativeButton("Keep Session", null)
            .show()
    }
}
