package com.example.openliberty.model

import androidx.compose.ui.graphics.Color

enum class VehicleType(
    val displayName: String,
    val maxSpeed: Float,
    val acceleration: Float,
    val handling: Float,
    val defaultColor: Color,
    val width: Float,
    val length: Float
) {
    BANSHEE("Banshee (Sports)", 28f, 18f, 1.4f, Color(0xFFEF4444), 2.2f, 4.4f),
    PICKUP("Bobcat Pickup", 18f, 11f, 1.0f, Color(0xFF2563EB), 2.3f, 4.8f),
    POLICE("LCPD Cruiser", 24f, 16f, 1.3f, Color(0xFF1E293B), 2.2f, 4.6f),
    TAXI("Cabbie", 20f, 13f, 1.1f, Color(0xFFFACC15), 2.2f, 4.5f),
    SENTINEL("Mafia Sentinel", 22f, 15f, 1.2f, Color(0xFF0F172A), 2.2f, 4.6f)
}

data class VehicleInstance(
    val id: String,
    val type: VehicleType,
    var x: Float,
    var y: Float,
    var heading: Float, // In radians
    var speed: Float = 0f,
    var steeringAngle: Float = 0f,
    var headlightsOn: Boolean = false,
    var cabinLightOn: Boolean = false,
    var sirenOn: Boolean = false,
    var isDrifting: Boolean = false,
    var health: Float = 100f,
    val color: Color = type.defaultColor,
    var wheelAngle: Float = 0f,
    var pitch: Float = 0f,
    var roll: Float = 0f
) {
    fun update(delta: Float) {
        // Forward/backward translation based on heading and speed
        x += (kotlin.math.sin(heading) * speed * delta)
        y += (-kotlin.math.cos(heading) * speed * delta)

        // Turn rate proportional to steering and speed
        if (kotlin.math.abs(speed) > 0.1f) {
            val turnFactor = if (speed >= 0) 1f else -1f
            heading += steeringAngle * turnFactor * (speed / type.maxSpeed) * type.handling * delta * 2.5f
        }

        // Wheel spin proportional to forward/reverse movement
        wheelAngle = (wheelAngle + speed * delta * 5.5f) % (2f * Math.PI.toFloat())

        // Dynamic body roll into sharp turns
        val targetRoll = -steeringAngle * (kotlin.math.abs(speed) / type.maxSpeed) * 0.12f
        roll += (targetRoll - roll) * (delta * 6f).coerceAtMost(1f)

        // Dynamic pitch (nose dip under braking, lift under acceleration)
        val targetPitch = if (speed > 1f) 0.035f else if (speed < -1f) -0.045f else 0f
        pitch += (targetPitch - pitch) * (delta * 5f).coerceAtMost(1f)

        // Drifting condition: high speed and sharp steer
        isDrifting = kotlin.math.abs(steeringAngle) > 0.45f && kotlin.math.abs(speed) > type.maxSpeed * 0.45f
    }
}
