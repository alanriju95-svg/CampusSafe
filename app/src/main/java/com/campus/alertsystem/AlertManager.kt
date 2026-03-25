package com.campus.alertsystem

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Singleton that manages the in-memory alert state.
 * Thread-safe access using @Synchronized.
 */
object AlertManager {

    private var activeAlertType: AlertType? = null
    private var alertTimestamp: Long = 0L
    private var alertLocation: String = ""

    private val dateFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

    @Synchronized
    fun setAlert(type: AlertType, location: String = "") {
        activeAlertType = type
        alertTimestamp = System.currentTimeMillis()
        alertLocation = location
    }

    @Synchronized
    fun clearAlert() {
        activeAlertType = null
        alertTimestamp = 0L
        alertLocation = ""
    }

    @Synchronized
    fun getActiveAlert(): AlertType? = activeAlertType

    @Synchronized
    fun getAlertTimestamp(): Long = alertTimestamp

    @Synchronized
    fun getAlertLocation(): String = alertLocation

    fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) {
            dateFormat.format(Date(timestamp))
        } else {
            "--:--:--"
        }
    }

    @Synchronized
    fun isAlertActive(): Boolean = activeAlertType != null
}
