package com.example.openliberty.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.model.GraphicsQuality
import com.example.openliberty.model.TouchControlMode
import com.example.openliberty.ui.theme.LibertyAmber
import com.example.openliberty.ui.theme.LibertyCyan
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val settings = state.settings

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Options & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("options_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0A0D14),
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Audio Section
            SettingsSection(title = "AUDIO SETTINGS") {
                SettingSlider(
                    title = "Master Volume",
                    value = settings.masterVolume,
                    onValueChange = { viewModel.updateSettings(settings.copy(masterVolume = it)) }
                )
                SettingSlider(
                    title = "Radio Music Volume",
                    value = settings.radioVolume,
                    onValueChange = { viewModel.updateSettings(settings.copy(radioVolume = it)) }
                )
                SettingSlider(
                    title = "Sound Effects (SFX)",
                    value = settings.sfxVolume,
                    onValueChange = { viewModel.updateSettings(settings.copy(sfxVolume = it)) }
                )
            }

            // Video & Environment Section
            SettingsSection(title = "VIDEO & ENVIRONMENT") {
                SettingSwitch(
                    title = "Night Mode Lighting",
                    subtitle = "Toggles dark sky and dynamic streetlight illumination",
                    checked = settings.isNightMode,
                    onCheckedChange = { viewModel.toggleNightMode() }
                )

                SettingSwitch(
                    title = "Show Performance FPS",
                    subtitle = "Displays real-time frame rate in HUD",
                    checked = settings.showFps,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(showFps = it)) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Graphics Quality Preset", fontSize = 13.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (q in GraphicsQuality.values()) {
                            FilterChip(
                                selected = settings.graphicsQuality == q,
                                onClick = { viewModel.updateSettings(settings.copy(graphicsQuality = q)) },
                                label = { Text(q.name, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LibertyAmber,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            // Controls Section
            SettingsSection(title = "CONTROLS & STEERING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Steering Input Type", fontSize = 13.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = settings.touchControlMode == TouchControlMode.ANALOG_JOYSTICK,
                            onClick = { viewModel.updateSettings(settings.copy(touchControlMode = TouchControlMode.ANALOG_JOYSTICK)) },
                            label = { Text("Joystick", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LibertyCyan, selectedLabelColor = Color.Black)
                        )
                        FilterChip(
                            selected = settings.touchControlMode == TouchControlMode.BUTTONS_DPAD,
                            onClick = { viewModel.updateSettings(settings.copy(touchControlMode = TouchControlMode.BUTTONS_DPAD)) },
                            label = { Text("Arrows", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LibertyCyan, selectedLabelColor = Color.Black)
                        )
                    }
                }

                SettingSlider(
                    title = "Steering Sensitivity",
                    value = settings.steeringSensitivity,
                    range = 0.5f..2.0f,
                    onValueChange = { viewModel.updateSettings(settings.copy(steeringSensitivity = it)) }
                )

                SettingSlider(
                    title = "Free-Fly Camera Speed",
                    value = settings.freeFlySpeedMultiplier,
                    range = 0.5f..3.0f,
                    onValueChange = { viewModel.updateSettings(settings.copy(freeFlySpeedMultiplier = it)) }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131926)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LibertyAmber, letterSpacing = 1.sp)
            content()
        }
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, color = Color.White)
            Text("${(value * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = LibertyAmber,
                activeTrackColor = LibertyAmber,
                inactiveTrackColor = Color(0xFF334155)
            )
        )
    }
}

@Composable
fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LibertyAmber,
                checkedTrackColor = LibertyAmber.copy(alpha = 0.5f)
            )
        )
    }
}
