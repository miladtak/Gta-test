package com.example.openliberty.model

data class GameSettings(
    val masterVolume: Float = 0.8f,
    val radioVolume: Float = 0.7f,
    val sfxVolume: Float = 0.85f,
    val isNightMode: Boolean = false,
    val showFps: Boolean = true,
    val touchControlMode: TouchControlMode = TouchControlMode.ANALOG_JOYSTICK,
    val steeringSensitivity: Float = 1.0f,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
    val freeFlySpeedMultiplier: Float = 1.5f
)

enum class TouchControlMode {
    ANALOG_JOYSTICK,
    BUTTONS_DPAD
}

enum class GraphicsQuality {
    LOW,
    MEDIUM,
    HIGH
}
