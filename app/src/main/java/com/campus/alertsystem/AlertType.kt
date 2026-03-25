package com.campus.alertsystem

/**
 * Enum representing the three types of campus alerts.
 * Each type carries its associated color resource, icon resource, label, and safety instructions.
 */
enum class AlertType(
    val colorResId: Int,
    val iconResId: Int,
    val label: String,
    val instructionsResId: Int
) {
    FIRE(
        colorResId = R.color.fire_alert,
        iconResId = R.drawable.ic_fire,
        label = "FIRE ALERT",
        instructionsResId = R.string.fire_instructions
    ),
    MEDICAL(
        colorResId = R.color.medical_alert,
        iconResId = R.drawable.ic_medical,
        label = "MEDICAL ALERT",
        instructionsResId = R.string.medical_instructions
    ),
    SECURITY(
        colorResId = R.color.security_alert,
        iconResId = R.drawable.ic_security,
        label = "SECURITY ALERT",
        instructionsResId = R.string.security_instructions
    )
}
