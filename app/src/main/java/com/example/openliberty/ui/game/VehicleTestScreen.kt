package com.example.openliberty.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.model.VehicleType
import com.example.openliberty.ui.theme.LibertyAmber
import com.example.openliberty.ui.theme.LibertyCyan
import com.example.openliberty.ui.theme.LibertyRed
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun VehicleTestScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val activeVehicle = state.vehicles.find { it.id == state.currentVehicleId }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        // True 3D World & Vehicle
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

        // Telemetry Overlay Panel (Top Center)
        Surface(
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("VEHICLE DYNAMICS TELEMETRY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LibertyAmber)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val speedMph = ((activeVehicle?.speed ?: 0f) * 2.23694f).roundToInt()
                    TelemetryMetric("SPEED", "$speedMph MPH", LibertyCyan)
                    TelemetryMetric("STEER", "%.1f°".format(Math.toDegrees((activeVehicle?.steeringAngle ?: 0f).toDouble())), Color.White)
                    TelemetryMetric("DRIFT", if (activeVehicle?.isDrifting == true) "YES" else "NO", if (activeVehicle?.isDrifting == true) LibertyRed else Color(0xFF64748B))
                    TelemetryMetric("LIGHTS", if (activeVehicle?.headlightsOn == true) "ON" else "OFF", if (activeVehicle?.headlightsOn == true) LibertyAmber else Color(0xFF64748B))
                }
            }
        }

        // Vehicle Switcher Chips (Bottom Top of Controls)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 65.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                modifier = Modifier.size(36.dp).background(Color(0xBB1E293B), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            for (type in VehicleType.values().take(3)) {
                AssistChip(
                    onClick = { viewModel.spawnCar(type) },
                    label = { Text(type.displayName.split(" ").first(), fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xBB1E293B),
                        labelColor = Color.White
                    )
                )
            }
        }

        // Standard Game Controls HUD
        GameControls(
            isInVehicle = true,
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
            onJump = { },
            onSprint = { },
            onToggleVehicle = { viewModel.toggleVehicleEnterExit() },
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

@Composable
fun TelemetryMetric(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 8.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = valueColor)
    }
}
