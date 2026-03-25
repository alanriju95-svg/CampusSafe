package com.campus.alertsystem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * Main launcher activity with panel selection buttons and emergency contacts.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<LinearLayout>(R.id.btnAdminPanel).setOnClickListener {
            startActivity(Intent(this, AdminPanelActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnStudentPanel).setOnClickListener {
            startActivity(Intent(this, StudentPanelActivity::class.java))
        }

        findViewById<Button>(R.id.btnCallSecurity).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0480-2730702")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCallMedical).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:emer@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "EMERGENCY ALERT")
            }
            startActivity(intent)
        }
    }
}
