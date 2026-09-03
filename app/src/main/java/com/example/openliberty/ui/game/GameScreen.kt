package com.example.openliberty.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val activeVehicle = state.vehicles.find { it.id == state.currentVehicleId }

    Box(modifier = modifier.fillMaxSize()) {
        // 3D Canvas World
        World3DRenderer(
            player = state.player,
            vehicles = state.vehicles,
            currentVehicleId = state.currentVehicleId,
            isNight = state.isNight,
            timeOfDayHours = state.timeOfDayHours,
            cameraMode = state.cameraMode,
            orbitYaw = state.orbitYaw,
            orbitPitch = state.orbitPitch,
            onOrbitDrag = viewModel::onOrbitDrag,
            onResetOrbit = viewModel::resetOrbit,
            onCycleCamera = viewModel::cycleCameraMode,
            modifier = Modifier.fillMaxSize()
        )

        // Radar Mini-Map (Top Left, under status bar)
        MiniMap(
            playerX = if (state.player.isInVehicle && activeVehicle != null) activeVehicle.x else state.player.x,
            playerY = if (state.player.isInVehicle && activeVehicle != null) activeVehicle.y else state.player.y,
            playerHeading = if (state.player.isInVehicle && activeVehicle != null) activeVehicle.heading else state.player.heading,
            vehicles = state.vehicles,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 14.dp, top = 56.dp)
        )

        // Overlay Game Controls & HUD
        GameControls(
            isInVehicle = state.player.isInVehicle,
            activeVehicle = activeVehicle,
            activeStation = state.activeRadioStation,
            isRadioPlaying = state.isRadioPlaying,
            isNight = state.isNight,
            wantedLevel = state.wantedLevel,
            fps = state.fps,
            touchControlMode = state.settings.touchControlMode,
            onSteer = viewModel::setSteering,
            onThrottle = viewModel::setThrottle,
            onHandbrake = viewModel::setHandbrake,
            onJump = viewModel::jumpPlayer,
            onSprint = viewModel::setSprint,
            onToggleVehicle = viewModel::toggleVehicleEnterExit,
            onToggleHeadlights = viewModel::toggleHeadlights,
            onToggleCabinLight = viewModel::toggleCabinLight,
            onToggleSiren = viewModel::toggleSiren,
            onHonkHorn = viewModel::honkHorn,
            onCycleRadio = viewModel::cycleRadioStation,
            onToggleRadioPlay = viewModel::toggleRadioPlayback,
            onToggleNight = viewModel::toggleNightMode,
            onCycleCamera = viewModel::cycleCameraMode,
            onSpawnVehicle = viewModel::spawnCar,
            onOpenMenu = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
            modifier = Modifier.fillMaxSize()
        )
    }
}
