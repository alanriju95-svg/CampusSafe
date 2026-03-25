package com.campus.alertsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

/**
 * Admin Panel Activity — dispatches Fire, Medical, and Security alerts.
 * Each button sets the alert in AlertManager and starts the AlertForegroundService.
 */
class AdminPanelActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var etLocation: EditText

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        tvStatus = findViewById(R.id.tvStatus)
        etLocation = findViewById(R.id.etLocation)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }

        findViewById<Button>(R.id.btnFireAlert).setOnClickListener {
            dispatchAlert(AlertType.FIRE)
        }

        findViewById<Button>(R.id.btnMedicalAlert).setOnClickListener {
            dispatchAlert(AlertType.MEDICAL)
        }

        findViewById<Button>(R.id.btnSecurityAlert).setOnClickListener {
            dispatchAlert(AlertType.SECURITY)
        }
    }

    private fun dispatchAlert(alertType: AlertType) {
        val location = etLocation.text.toString().trim()
        
        // Set the alert in the shared manager
        AlertManager.setAlert(alertType, location)

        // Start the foreground service
        val serviceIntent = Intent(this, AlertForegroundService::class.java).apply {
            putExtra(AlertForegroundService.EXTRA_ALERT_TYPE, alertType.name)
        }
        ContextCompat.startForegroundService(this, serviceIntent)

        // Update status text
        val timestamp = AlertManager.formatTimestamp(AlertManager.getAlertTimestamp())
        tvStatus.text = getString(R.string.alert_dispatched, alertType.label, timestamp)
        tvStatus.setTextColor(ContextCompat.getColor(this, alertType.colorResId))

        Toast.makeText(this, "${alertType.label} dispatched! Returning in 3 seconds...", Toast.LENGTH_SHORT).show()

        // Wait 3 seconds and then go back to the main dashboard
        tvStatus.postDelayed({
            if (!isFinishing) {
                finish()
            }
        }, 3000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Notification permission is required for alert service",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
