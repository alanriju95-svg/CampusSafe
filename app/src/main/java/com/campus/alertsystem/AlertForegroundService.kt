package com.campus.alertsystem

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service that polls the in-memory alert flag every 10 seconds
 * and dispatches a high-priority broadcast with alert type and timestamp.
 */
class AlertForegroundService : Service() {

    companion object {
        const val EXTRA_ALERT_TYPE = "extra_alert_type"
        const val ACTION_CAMPUS_ALERT = "com.campus.alertsystem.CAMPUS_ALERT"
        const val ACTION_CLEAR_ALERT = "com.campus.alertsystem.CLEAR_ALERT"
        const val EXTRA_BROADCAST_ALERT_TYPE = "broadcast_alert_type"
        const val EXTRA_BROADCAST_TIMESTAMP = "broadcast_timestamp"

        private const val CHANNEL_ID = "alert_service_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 10_000L
    }

    private var handlerThread: HandlerThread? = null
    private var pollHandler: Handler? = null
    private var isRunning = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            
            val activeAlert = AlertManager.getActiveAlert()
            if (activeAlert != null) {
                // Send high-priority broadcast
                val broadcastIntent = Intent(ACTION_CAMPUS_ALERT).apply {
                    putExtra(EXTRA_BROADCAST_ALERT_TYPE, activeAlert.name)
                    putExtra(EXTRA_BROADCAST_TIMESTAMP, AlertManager.getAlertTimestamp())
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                    setPackage(packageName)
                }
                sendBroadcast(broadcastIntent)

                // Schedule next poll
                pollHandler?.postDelayed(this, POLL_INTERVAL_MS)
            } else {
                // No active alert, stop service
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alertTypeName = intent?.getStringExtra(EXTRA_ALERT_TYPE) ?: return START_NOT_STICKY

        val alertType = try {
            AlertType.valueOf(alertTypeName)
        } catch (e: IllegalArgumentException) {
            return START_NOT_STICKY
        }

        // Start foreground IMMEDIATELY
        val notification = buildNotification(alertType)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If it fails to start foreground, we shouldn't continue
            stopSelf()
            return START_NOT_STICKY
        }

        // Stop any previous polling
        stopPolling()

        // Start polling on a background thread
        isRunning = true
        handlerThread = HandlerThread("AlertPollThread").also { it.start() }
        pollHandler = Handler(handlerThread!!.looper)

        // Send the first broadcast immediately, then poll every 10s
        pollHandler?.post(pollRunnable)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopPolling()
        super.onDestroy()
    }

    private fun stopPolling() {
        isRunning = false
        pollHandler?.removeCallbacks(pollRunnable)
        handlerThread?.quitSafely()
        handlerThread = null
        pollHandler = null
    }

    private fun buildNotification(alertType: AlertType): Notification {
        val timestamp = AlertManager.formatTimestamp(AlertManager.getAlertTimestamp())
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title, alertType.label))
            .setContentText(getString(R.string.notification_text, timestamp))
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.alert_channel_description)
            setShowBadge(true)
            setSound(null, null) // We handle sound in Activity
            enableVibration(false) // We handle vibration in Activity
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
