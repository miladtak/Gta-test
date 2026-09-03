package com.example.openliberty.model

data class PlayerState(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f, // Altitude / jump height
    var vz: Float = 0f, // Vertical velocity
    var heading: Float = 0f, // Orientation in radians
    var isWalking: Boolean = false,
    var isSprinting: Boolean = false,
    var isJumping: Boolean = false,
    var isInVehicle: Boolean = false,
    var currentVehicleId: String? = null,
    var health: Int = 100,
    var armor: Int = 100,
    var sprintEnergy: Float = 100f,
    var walkCyclePhase: Float = 0f
) {
    val speedWalk = 4.0f
    val speedRun = 7.5f
    val jumpForce = 5.5f
    val gravity = 14.0f

    fun update(delta: Float) {
        if (!isInVehicle) {
            // Apply gravity if in air
            if (z > 0f || vz != 0f) {
                z += vz * delta
                vz -= gravity * delta
                if (z <= 0f) {
                    z = 0f
                    vz = 0f
                    isJumping = false
                }
            }

            // Sprint energy replenishment
            if (isSprinting) {
                sprintEnergy = (sprintEnergy - delta * 20f).coerceAtLeast(0f)
            } else {
                sprintEnergy = (sprintEnergy + delta * 15f).coerceAtMost(100f)
            }

            // 3D stride animation phase
            if (isWalking) {
                val cycleSpeed = if (isSprinting) 13f else 7.5f
                walkCyclePhase = (walkCyclePhase + delta * cycleSpeed) % (2f * Math.PI.toFloat())
            } else {
                walkCyclePhase = 0f
            }
        }
    }

    fun jump() {
        if (!isInVehicle && z <= 0.05f) {
            vz = jumpForce
            isJumping = true
        }
    }
}
