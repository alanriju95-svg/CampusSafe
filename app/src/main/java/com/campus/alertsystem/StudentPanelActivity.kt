package com.campus.alertsystem

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

/**
 * Student Panel Activity — receives alert broadcasts and displays animated alert overlay.
 */
class StudentPanelActivity : AppCompatActivity() {

    // Views
    private lateinit var rootFrame: View
    private lateinit var normalContent: ConstraintLayout
    private lateinit var alertOverlay: ConstraintLayout
    private lateinit var ivAlertIcon: ImageView
    private lateinit var tvAlertType: TextView
    private lateinit var tvAlertLocation: TextView
    private lateinit var tvAlertTimestamp: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var btnDismiss: Button
    private lateinit var tvLastAlert: TextView
    private lateinit var safeStatusLayout: LinearLayout
    private lateinit var btnIAmSafe: Button
    private lateinit var btnINeedHelp: Button

    // Animators
    private var colorAnimator: ObjectAnimator? = null
    private var blinkAnimator: ObjectAnimator? = null
    private var alertAnimatorSet: AnimatorSet? = null
    private var pulseAnimator: AnimatorSet? = null

    // Audio & Haptics
    private var vibrator: Vibrator? = null
    private var ringtone: Ringtone? = null

    // Normal background color
    private val normalBgColor by lazy { ContextCompat.getColor(this, R.color.normal_bg) }

    // Broadcast receivers
    private val alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val alertTypeName = intent.getStringExtra(
                AlertForegroundService.EXTRA_BROADCAST_ALERT_TYPE
            ) ?: return
            val timestamp = intent.getLongExtra(
                AlertForegroundService.EXTRA_BROADCAST_TIMESTAMP, 0L
            )

            val alertType = try {
                AlertType.valueOf(alertTypeName)
            } catch (e: IllegalArgumentException) {
                return
            }

            showAlert(alertType, timestamp)
        }
    }

    private val clearReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            clearAlert()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_panel)

        val toolbar = findViewById<MaterialToolbar>(R.id.studentToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize audio & haptics safely
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Bind views
        rootFrame = findViewById(R.id.rootFrame)
        normalContent = findViewById(R.id.normalContent)
        alertOverlay = findViewById(R.id.alertOverlay)
        ivAlertIcon = findViewById(R.id.ivAlertIcon)
        tvAlertType = findViewById(R.id.tvAlertType)
        tvAlertLocation = findViewById(R.id.tvAlertLocation)
        tvAlertTimestamp = findViewById(R.id.tvAlertTimestamp)
        tvInstructions = findViewById(R.id.tvInstructions)
        btnDismiss = findViewById(R.id.btnDismiss)
        tvLastAlert = findViewById(R.id.tvLastAlert)
        safeStatusLayout = findViewById(R.id.safeStatusLayout)
        btnIAmSafe = findViewById(R.id.btnIAmSafe)
        btnINeedHelp = findViewById(R.id.btnINeedHelp)

        // Dismiss button broadcasts CLEAR_ALERT
        btnDismiss.setOnClickListener {
            // Clear the alert in manager
            AlertManager.clearAlert()

            // Send CLEAR_ALERT broadcast
            val clearIntent = Intent(AlertForegroundService.ACTION_CLEAR_ALERT).apply {
                setPackage(packageName)
            }
            sendBroadcast(clearIntent)

            // Also clear locally
            clearAlert()
            
            // Show safe/unsafe buttons
            safeStatusLayout.visibility = View.VISIBLE
        }
        
        btnIAmSafe.setOnClickListener {
            Toast.makeText(this, "Status updated: You are safe.", Toast.LENGTH_SHORT).show()
            safeStatusLayout.visibility = View.GONE
        }
        
        btnINeedHelp.setOnClickListener {
            Toast.makeText(this, "Emergency responders have been notified of your request for help.", Toast.LENGTH_LONG).show()
            safeStatusLayout.visibility = View.GONE
        }
        
        // Check if there's an active alert already
        val activeAlert = AlertManager.getActiveAlert()
        if (activeAlert != null) {
            showAlert(activeAlert, AlertManager.getAlertTimestamp())
        }
    }

    override fun onResume() {
        super.onResume()

        // Register receivers (foreground only)
        val alertFilter = IntentFilter(AlertForegroundService.ACTION_CAMPUS_ALERT)
        val clearFilter = IntentFilter(AlertForegroundService.ACTION_CLEAR_ALERT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alertReceiver, alertFilter, Context.RECEIVER_NOT_EXPORTED)
            registerReceiver(clearReceiver, clearFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(alertReceiver, alertFilter)
            registerReceiver(clearReceiver, clearFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(alertReceiver)
            unregisterReceiver(clearReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    /**
     * Show the alert with all required animations.
     */
    private fun showAlert(alertType: AlertType, timestamp: Long) {
        val alertColor = ContextCompat.getColor(this, alertType.colorResId)

        // Hide safe status if a new alert comes
        safeStatusLayout.visibility = View.GONE

        // Cancel any previous animations
        cancelAllAnimators()

        // === 1. Background color transition ===
        colorAnimator = ObjectAnimator.ofArgb(
            rootFrame, "backgroundColor", normalBgColor, alertColor
        ).apply {
            duration = 500
        }

        // === 2. Blink animation ===
        blinkAnimator = ObjectAnimator.ofFloat(
            rootFrame, "alpha", 1f, 0f, 1f
        ).apply {
            duration = 300
            repeatCount = 5
        }

        alertAnimatorSet = AnimatorSet().apply {
            playTogether(colorAnimator, blinkAnimator)
            start()
        }

        // === 3. Show overlay with slide in animation ===
        alertOverlay.visibility = View.VISIBLE
        alertOverlay.alpha = 0f
        alertOverlay.translationY = 500f
        alertOverlay.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Set overlay content
        ivAlertIcon.setImageResource(alertType.iconResId)
        tvAlertType.text = alertType.label
        
        val location = AlertManager.getAlertLocation()
        if (location.isNotEmpty()) {
            tvAlertLocation.text = getString(R.string.location_label, location)
            tvAlertLocation.visibility = View.VISIBLE
        } else {
            tvAlertLocation.visibility = View.GONE
        }

        tvAlertTimestamp.text = getString(
            R.string.alert_timestamp,
            AlertManager.formatTimestamp(timestamp)
        )
        tvInstructions.text = getString(alertType.instructionsResId)

        // === 4. Pulse animation on icon ===
        val scaleX = ObjectAnimator.ofFloat(ivAlertIcon, "scaleX", 1f, 1.3f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(ivAlertIcon, "scaleY", 1f, 1.3f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        pulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }

        // === 5. Audio and Haptics ===
        try {
            if (ringtone == null) {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone?.isLooping = true
                }
            }
            if (ringtone?.isPlaying == false) {
                ringtone?.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear alert: cancel all animators, reset all UI states.
     */
    private fun clearAlert() {
        // Cancel all animators
        cancelAllAnimators()

        // Reset background
        rootFrame.setBackgroundColor(normalBgColor)
        rootFrame.alpha = 1f

        // Hide overlay with animation
        if (alertOverlay.visibility == View.VISIBLE) {
            alertOverlay.animate()
                .alpha(0f)
                .translationY(500f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    alertOverlay.visibility = View.GONE
                }
                .start()
        } else {
            alertOverlay.visibility = View.GONE
        }

        // Reset icon scale
        ivAlertIcon.scaleX = 1f
        ivAlertIcon.scaleY = 1f

        // Update last alert info
        tvLastAlert.text = getString(R.string.no_active_alerts)
    }

    private fun cancelAllAnimators() {
        colorAnimator?.cancel()
        blinkAnimator?.cancel()
        alertAnimatorSet?.cancel()
        pulseAnimator?.cancel()

        colorAnimator = null
        blinkAnimator = null
        alertAnimatorSet = null
        pulseAnimator = null

        // Stop haptics & audio
        try {
            ringtone?.stop()
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        cancelAllAnimators()
        super.onDestroy()
    }
}
