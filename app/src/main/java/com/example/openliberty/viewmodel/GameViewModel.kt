package com.example.openliberty.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openliberty.audio.SoundSynth
import com.example.openliberty.data.RenderWareSampleData
import com.example.openliberty.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class AppScreen {
    MAIN_MENU,
    OPEN_WORLD,
    VEHICLE_TEST,
    RENDERWARE_INSPECTOR,
    OPTIONS,
    CREDITS
}

enum class CameraMode(val displayName: String) {
    CHASE_CAM("3D Chase"),
    HOOD_CAM("3D Cockpit"),
    ORBIT_CAM("3D 360° Orbit"),
    ISOMETRIC_CAM("3D Isometric"),
    FREE_FLY("3D FlyCam")
}

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val player: PlayerState = PlayerState(x = 0f, y = 2f, z = 0f),
    val vehicles: List<VehicleInstance> = emptyList(),
    val currentVehicleId: String? = null,
    val settings: GameSettings = GameSettings(),
    val activeRadioStation: RadioStation = RadioStation.HEAD_RADIO,
    val isRadioPlaying: Boolean = true,
    val isNight: Boolean = false,
    val timeOfDayHours: Float = 14.0f, // 14:00 (afternoon)
    val cameraMode: CameraMode = CameraMode.CHASE_CAM,
    val orbitYaw: Float = 0f,
    val orbitPitch: Float = 14f,
    val wantedLevel: Int = 0,
    val fps: Int = 60,
    // Free fly camera state
    val flyCamX: Float = 0f,
    val flyCamY: Float = 0f,
    val flyCamAltitude: Float = 25f,
    val flyCamPitch: Float = -45f,
    val flyCamYaw: Float = 0f,
    // RenderWare Inspector State
    val inspectorTab: Int = 0, // 0=TXD, 1=DFF, 2=IPL/Map, 3=Flycam
    val selectedTextureIndex: Int = 0,
    val selectedDffIndex: Int = 0,
    val isWireframeEnabled: Boolean = false,
    val modelRotX: Float = 20f,
    val modelRotY: Float = -35f,
    val modelZoom: Float = 1.0f
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val synth = SoundSynth()
    private var gameLoopJob: Job? = null

    // Controller input states
    private var inputSteer: Float = 0f
    private var inputThrottle: Float = 0f
    private var inputHandbrake: Boolean = false

    init {
        // Initialize default vehicles
        val initialVehicles = listOf(
            VehicleInstance(
                id = "player_banshee",
                type = VehicleType.BANSHEE,
                x = 0f,
                y = 0f,
                heading = 0f,
                color = Color(0xFFEF4444)
            ),
            VehicleInstance(
                id = "pickup_01",
                type = VehicleType.PICKUP,
                x = -12f,
                y = 18f,
                heading = 1.57f,
                color = Color(0xFF2563EB)
            ),
            VehicleInstance(
                id = "police_01",
                type = VehicleType.POLICE,
                x = 14f,
                y = -22f,
                heading = -1.57f,
                sirenOn = false
            ),
            VehicleInstance(
                id = "taxi_01",
                type = VehicleType.TAXI,
                x = -24f,
                y = -8f,
                heading = 3.14f
            )
        )

        _uiState.value = _uiState.value.copy(
            vehicles = initialVehicles,
            currentVehicleId = "player_banshee",
            player = _uiState.value.player.copy(isInVehicle = true, currentVehicleId = "player_banshee")
        )

        synth.start()
        updateSynthVolumes()
        startGameLoop()
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
        if (screen == AppScreen.VEHICLE_TEST) {
            // Place player in test car in test arena
            val testVehicle = VehicleInstance(
                id = "test_chamber_car",
                type = VehicleType.BANSHEE,
                x = 0f,
                y = 0f,
                heading = 0f,
                color = Color(0xFF10B981)
            )
            _uiState.value = _uiState.value.copy(
                vehicles = listOf(testVehicle),
                currentVehicleId = testVehicle.id,
                player = _uiState.value.player.copy(isInVehicle = true, currentVehicleId = testVehicle.id)
            )
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            var frameCount = 0
            var fpsTimer = System.currentTimeMillis()

            while (isActive) {
                val now = System.nanoTime()
                val delta = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                lastTime = now

                frameCount++
                if (System.currentTimeMillis() - fpsTimer >= 1000) {
                    _uiState.value = _uiState.value.copy(fps = frameCount)
                    frameCount = 0
                    fpsTimer = System.currentTimeMillis()
                }

                updateSimulation(delta)
                delay(16) // ~60fps
            }
        }
    }

    private fun updateSimulation(delta: Float) {
        val currentState = _uiState.value
        val updatedVehicles = currentState.vehicles.map { it.copy() }
        val player = currentState.player.copy()

        player.update(delta)

        val activeVehicle = updatedVehicles.find { it.id == currentState.currentVehicleId }

        if (player.isInVehicle && activeVehicle != null) {
            // Apply steering
            val targetSteer = inputSteer * activeVehicle.type.handling * currentState.settings.steeringSensitivity
            activeVehicle.steeringAngle = lerp(activeVehicle.steeringAngle, targetSteer, delta * 8f)

            // Apply acceleration & braking
            if (inputHandbrake) {
                activeVehicle.speed = lerp(activeVehicle.speed, 0f, delta * 5f)
                activeVehicle.isDrifting = activeVehicle.speed > 5f
            } else if (inputThrottle > 0f) {
                val accel = activeVehicle.type.acceleration * inputThrottle
                activeVehicle.speed = (activeVehicle.speed + accel * delta).coerceAtMost(activeVehicle.type.maxSpeed)
            } else if (inputThrottle < 0f) {
                val brakeDecel = activeVehicle.type.acceleration * 1.2f * (-inputThrottle)
                activeVehicle.speed = (activeVehicle.speed - brakeDecel * delta).coerceAtLeast(-activeVehicle.type.maxSpeed * 0.45f)
            } else {
                // Natural engine rolling friction
                activeVehicle.speed = lerp(activeVehicle.speed, 0f, delta * 1.5f)
            }

            activeVehicle.update(delta)

            // Sync player position with car
            player.x = activeVehicle.x
            player.y = activeVehicle.y
            player.heading = activeVehicle.heading

            // Audio update
            val speedFraction = (kotlin.math.abs(activeVehicle.speed) / activeVehicle.type.maxSpeed).coerceIn(0f, 1f)
            synth.isEngineRunning = true
            synth.currentSpeedFraction = speedFraction
            synth.isTireScreeching = activeVehicle.isDrifting || (inputHandbrake && speedFraction > 0.2f)
            synth.isSirenActive = activeVehicle.sirenOn || currentState.wantedLevel > 0
        } else {
            // Player on foot
            synth.isEngineRunning = false
            synth.currentSpeedFraction = 0f
            synth.isTireScreeching = false
            synth.isSirenActive = currentState.wantedLevel > 0

            if (inputSteer != 0f || inputThrottle != 0f) {
                player.isWalking = true
                val moveSpeed = if (player.isSprinting && player.sprintEnergy > 1f) player.speedRun else player.speedWalk
                val angle = kotlin.math.atan2(inputSteer, -inputThrottle)
                player.heading = angle
                player.x += sin(angle) * moveSpeed * delta
                player.y += -cos(angle) * moveSpeed * delta
            } else {
                player.isWalking = false
            }
        }

        // Time of day progression
        var timeOfDay = currentState.timeOfDayHours + (delta * 0.05f) // slow progression
        if (timeOfDay >= 24f) timeOfDay -= 24f
        val isNightCalculated = currentState.settings.isNightMode || (timeOfDay < 6f || timeOfDay > 19f)

        _uiState.value = currentState.copy(
            vehicles = updatedVehicles,
            player = player,
            timeOfDayHours = timeOfDay,
            isNight = isNightCalculated
        )
    }

    // Input handlers
    fun setSteering(value: Float) {
        inputSteer = value.coerceIn(-1f, 1f)
    }

    fun setThrottle(value: Float) {
        inputThrottle = value.coerceIn(-1f, 1f)
    }

    fun setHandbrake(active: Boolean) {
        inputHandbrake = active
    }

    fun jumpPlayer() {
        _uiState.value.player.jump()
    }

    fun setSprint(sprint: Boolean) {
        val player = _uiState.value.player
        player.isSprinting = sprint && player.sprintEnergy > 1f
    }

    fun toggleVehicleEnterExit() {
        val state = _uiState.value
        val player = state.player
        if (player.isInVehicle) {
            // Exit vehicle to the left
            val currentVehicle = state.vehicles.find { it.id == state.currentVehicleId }
            if (currentVehicle != null) {
                player.isInVehicle = false
                player.currentVehicleId = null
                player.x = currentVehicle.x - 2.5f
                player.y = currentVehicle.y
                _uiState.value = state.copy(player = player, currentVehicleId = null)
            }
        } else {
            // Check for nearest vehicle
            val nearest = state.vehicles.minByOrNull {
                val dx = it.x - player.x
                val dy = it.y - player.y
                dx * dx + dy * dy
            }
            if (nearest != null) {
                val dist = kotlin.math.hypot(nearest.x - player.x, nearest.y - player.y)
                if (dist < 5.0f) {
                    player.isInVehicle = true
                    player.currentVehicleId = nearest.id
                    player.x = nearest.x
                    player.y = nearest.y
                    _uiState.value = state.copy(player = player, currentVehicleId = nearest.id)
                }
            }
        }
    }

    fun toggleHeadlights() {
        val state = _uiState.value
        val activeVehicle = state.vehicles.find { it.id == state.currentVehicleId }
        if (activeVehicle != null) {
            activeVehicle.headlightsOn = !activeVehicle.headlightsOn
            _uiState.value = state.copy(vehicles = state.vehicles.map { if (it.id == activeVehicle.id) activeVehicle else it })
        }
    }

    fun toggleCabinLight() {
        val state = _uiState.value
        val activeVehicle = state.vehicles.find { it.id == state.currentVehicleId }
        if (activeVehicle != null) {
            activeVehicle.cabinLightOn = !activeVehicle.cabinLightOn
            _uiState.value = state.copy(vehicles = state.vehicles.map { if (it.id == activeVehicle.id) activeVehicle else it })
        }
    }

    fun toggleSiren() {
        val state = _uiState.value
        val activeVehicle = state.vehicles.find { it.id == state.currentVehicleId }
        if (activeVehicle != null) {
            activeVehicle.sirenOn = !activeVehicle.sirenOn
            _uiState.value = state.copy(vehicles = state.vehicles.map { if (it.id == activeVehicle.id) activeVehicle else it })
        }
    }

    fun honkHorn(honking: Boolean) {
        synth.isHornHonking = honking
    }

    fun toggleNightMode() {
        val current = _uiState.value.settings.isNightMode
        val updated = _uiState.value.settings.copy(isNightMode = !current)
        _uiState.value = _uiState.value.copy(
            settings = updated,
            isNight = !current,
            timeOfDayHours = if (!current) 23f else 14f
        )
    }

    fun spawnCar(type: VehicleType) {
        val state = _uiState.value
        val player = state.player
        val newId = "spawned_${System.currentTimeMillis()}"
        val spawnX = player.x + (if (player.isInVehicle) 4f else 3f)
        val spawnY = player.y + (if (player.isInVehicle) 4f else 3f)

        val newVehicle = VehicleInstance(
            id = newId,
            type = type,
            x = spawnX,
            y = spawnY,
            heading = player.heading,
            color = type.defaultColor
        )

        _uiState.value = state.copy(vehicles = state.vehicles + newVehicle)
    }

    fun cycleRadioStation() {
        val stations = RadioStation.values()
        val currentIndex = stations.indexOf(_uiState.value.activeRadioStation)
        val nextStation = stations[(currentIndex + 1) % stations.size]
        _uiState.value = _uiState.value.copy(activeRadioStation = nextStation)
        synth.activeStation = nextStation
    }

    fun toggleRadioPlayback() {
        val current = _uiState.value.isRadioPlaying
        _uiState.value = _uiState.value.copy(isRadioPlaying = !current)
        synth.isRadioPlaying = !current
    }

    fun cycleCameraMode() {
        val modes = CameraMode.values()
        val nextMode = modes[(_uiState.value.cameraMode.ordinal + 1) % modes.size]
        _uiState.value = _uiState.value.copy(cameraMode = nextMode)
    }

    fun onOrbitDrag(deltaX: Float, deltaY: Float) {
        val state = _uiState.value
        val newYaw = state.orbitYaw + deltaX * 0.005f
        val newPitch = (state.orbitPitch - deltaY * 0.2f).coerceIn(2f, 82f)
        _uiState.value = state.copy(orbitYaw = newYaw, orbitPitch = newPitch)
    }

    fun resetOrbit() {
        _uiState.value = _uiState.value.copy(orbitYaw = 0f, orbitPitch = 14f)
    }

    fun setWantedLevel(stars: Int) {
        val clamped = stars.coerceIn(0, 5)
        _uiState.value = _uiState.value.copy(wantedLevel = clamped)
        synth.isSirenActive = clamped > 0
    }

    fun updateSettings(newSettings: GameSettings) {
        _uiState.value = _uiState.value.copy(settings = newSettings)
        updateSynthVolumes()
    }

    private fun updateSynthVolumes() {
        val s = _uiState.value.settings
        synth.masterVolume = s.masterVolume
        synth.radioVolume = s.radioVolume
        synth.sfxVolume = s.sfxVolume
    }

    // Inspector functions
    fun setInspectorTab(tab: Int) {
        _uiState.value = _uiState.value.copy(inspectorTab = tab)
    }

    fun selectTexture(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTextureIndex = index)
    }

    fun selectDffModel(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedDffIndex = index,
            modelRotX = 20f,
            modelRotY = -35f,
            modelZoom = 1.0f
        )
    }

    fun toggleWireframe() {
        _uiState.value = _uiState.value.copy(isWireframeEnabled = !_uiState.value.isWireframeEnabled)
    }

    fun rotateModel(dx: Float, dy: Float) {
        _uiState.value = _uiState.value.copy(
            modelRotY = _uiState.value.modelRotY + dx * 0.5f,
            modelRotX = (_uiState.value.modelRotX + dy * 0.5f).coerceIn(-80f, 80f)
        )
    }

    fun zoomModel(deltaZoom: Float) {
        _uiState.value = _uiState.value.copy(
            modelZoom = (_uiState.value.modelZoom * deltaZoom).coerceIn(0.4f, 3.0f)
        )
    }

    // Free Fly controls
    fun panFlyCam(dx: Float, dy: Float) {
        val s = _uiState.value.settings.freeFlySpeedMultiplier
        _uiState.value = _uiState.value.copy(
            flyCamX = _uiState.value.flyCamX + dx * s * 0.15f,
            flyCamY = _uiState.value.flyCamY + dy * s * 0.15f
        )
    }

    fun changeFlyAltitude(dAltitude: Float) {
        _uiState.value = _uiState.value.copy(
            flyCamAltitude = (_uiState.value.flyCamAltitude + dAltitude).coerceIn(5f, 150f)
        )
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction.coerceIn(0f, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        synth.stop()
    }
}
