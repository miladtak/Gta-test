package com.example.openliberty.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.model.RadioStation
import com.example.openliberty.model.TouchControlMode
import com.example.openliberty.model.VehicleInstance
import com.example.openliberty.model.VehicleType
import com.example.openliberty.ui.theme.LibertyAmber
import com.example.openliberty.ui.theme.LibertyCyan
import com.example.openliberty.ui.theme.LibertyRed
import kotlin.math.roundToInt

@Composable
fun GameControls(
    isInVehicle: Boolean,
    activeVehicle: VehicleInstance?,
    activeStation: RadioStation,
    isRadioPlaying: Boolean,
    isNight: Boolean,
    wantedLevel: Int,
    fps: Int,
    touchControlMode: TouchControlMode,
    onSteer: (Float) -> Unit,
    onThrottle: (Float) -> Unit,
    onHandbrake: (Boolean) -> Unit,
    onJump: () -> Unit,
    onSprint: (Boolean) -> Unit,
    onToggleVehicle: () -> Unit,
    onToggleHeadlights: () -> Unit,
    onToggleCabinLight: () -> Unit,
    onToggleSiren: () -> Unit,
    onHonkHorn: (Boolean) -> Unit,
    onCycleRadio: () -> Unit,
    onToggleRadioPlay: () -> Unit,
    onToggleNight: () -> Unit,
    onCycleCamera: () -> Unit,
    onSpawnVehicle: (VehicleType) -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpawnSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        // Top Navigation & Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Menu & Camera Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .testTag("menu_button")
                        .size(42.dp)
                        .background(Color(0xBB1E293B), CircleShape)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Main Menu", tint = Color.White)
                }

                IconButton(
                    onClick = onCycleCamera,
                    modifier = Modifier
                        .testTag("camera_button")
                        .size(42.dp)
                        .background(Color(0xBB1E293B), CircleShape)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Camera Mode", tint = LibertyAmber)
                }

                IconButton(
                    onClick = onToggleNight,
                    modifier = Modifier
                        .testTag("night_toggle_button")
                        .size(42.dp)
                        .background(Color(0xBB1E293B), CircleShape)
                ) {
                    Icon(
                        if (isNight) Icons.Default.Nightlight else Icons.Default.WbSunny,
                        contentDescription = "Day/Night Toggle",
                        tint = if (isNight) LibertyCyan else LibertyAmber
                    )
                }
            }

            // Center: Radio HUD Banner
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(activeStation.colorHex).copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                color = Color(0xCC0F172A),
                onClick = onCycleRadio
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onToggleRadioPlay,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (isRadioPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Radio Mute",
                            tint = Color(activeStation.colorHex),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = activeStation.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(activeStation.colorHex)
                        )
                        Text(
                            text = activeStation.genre,
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Right: Wanted Stars & Speedometer
            Column(horizontalAlignment = Alignment.End) {
                // Wanted Level Stars
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (star in 1..5) {
                        val active = star <= wantedLevel
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $star",
                            tint = if (active) LibertyRed else Color(0x33FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Speedometer
                if (isInVehicle && activeVehicle != null) {
                    val mph = (kotlin.math.abs(activeVehicle.speed) * 2.23694f).roundToInt()
                    Surface(
                        color = Color(0xDD0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$mph",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = LibertyAmber
                            )
                            Text(
                                text = " MPH",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Vehicle Enter/Exit, Lights, Horn, Spawn)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Spawn Car FAB
                FilledTonalIconButton(
                    onClick = { showSpawnSheet = true },
                    modifier = Modifier.size(44.dp).testTag("spawn_car_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Spawn Car")
                }

                // Enter / Exit Vehicle Button
                FloatingActionButton(
                    onClick = onToggleVehicle,
                    containerColor = if (isInVehicle) LibertyRed else LibertyAmber,
                    contentColor = Color.Black,
                    modifier = Modifier.size(48.dp).testTag("vehicle_enter_exit_button")
                ) {
                    Icon(
                        imageVector = if (isInVehicle) Icons.Default.ExitToApp else Icons.Default.DirectionsCar,
                        contentDescription = if (isInVehicle) "Exit Car" else "Enter Car"
                    )
                }

                if (isInVehicle) {
                    // Headlights Toggle
                    IconButton(
                        onClick = onToggleHeadlights,
                        modifier = Modifier
                            .testTag("headlights_button")
                            .size(40.dp)
                            .background(
                                if (activeVehicle?.headlightsOn == true) LibertyAmber else Color(0x991E293B),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = "Headlights",
                            tint = if (activeVehicle?.headlightsOn == true) Color.Black else Color.White
                        )
                    }

                    // Cabin Dome Light Toggle
                    IconButton(
                        onClick = onToggleCabinLight,
                        modifier = Modifier
                            .testTag("cabin_light_button")
                            .size(40.dp)
                            .background(
                                if (activeVehicle?.cabinLightOn == true) Color(0xFFFBBF24) else Color(0x991E293B),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Cabin Light",
                            tint = if (activeVehicle?.cabinLightOn == true) Color.Black else Color.White
                        )
                    }

                    // Horn (Hold to honk)
                    Box(
                        modifier = Modifier
                            .testTag("horn_button")
                            .size(40.dp)
                            .background(Color(0x991E293B), CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        onHonkHorn(true)
                                        tryAwaitRelease()
                                        onHonkHorn(false)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = "Horn", tint = Color.White)
                    }
                }
            }
        }

        // Bottom Controls Bar (Steering & Pedals / Jump)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Side: Virtual Steering Controller
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                if (touchControlMode == TouchControlMode.ANALOG_JOYSTICK) {
                    VirtualJoystick(
                        onMove = { dx, dy ->
                            onSteer(dx)
                            if (!isInVehicle) {
                                onThrottle(-dy)
                            }
                        }
                    )
                } else {
                    // D-Pad Left / Right buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TouchHoldButton(
                            text = "<",
                            onHoldChange = { pressed -> onSteer(if (pressed) -1f else 0f) },
                            modifier = Modifier.size(56.dp)
                        )
                        TouchHoldButton(
                            text = ">",
                            onHoldChange = { pressed -> onSteer(if (pressed) 1f else 0f) },
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            // Right Side: Driving Pedals or Foot Actions
            if (isInVehicle) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Handbrake
                    TouchHoldButton(
                        text = "HAND\nBRAKE",
                        onHoldChange = onHandbrake,
                        backgroundColor = Color(0xBB475569),
                        textColor = Color.White,
                        modifier = Modifier.size(52.dp)
                    )

                    // Brake / Reverse Pedal
                    TouchHoldButton(
                        text = "BRAKE\nREV",
                        onHoldChange = { pressed -> onThrottle(if (pressed) -1f else 0f) },
                        backgroundColor = Color(0xBBEF4444),
                        textColor = Color.White,
                        modifier = Modifier.size(62.dp, 80.dp)
                    )

                    // Gas / Accelerate Pedal
                    TouchHoldButton(
                        text = "DRIVE\nGAS",
                        onHoldChange = { pressed -> onThrottle(if (pressed) 1f else 0f) },
                        backgroundColor = Color(0xBB10B981),
                        textColor = Color.Black,
                        modifier = Modifier.size(66.dp, 94.dp)
                    )
                }
            } else {
                // On Foot: Jump & Sprint buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Sprint
                    TouchHoldButton(
                        text = "RUN",
                        onHoldChange = onSprint,
                        backgroundColor = Color(0xBB0284C7),
                        textColor = Color.White,
                        modifier = Modifier.size(60.dp)
                    )

                    // Jump
                    IconButton(
                        onClick = onJump,
                        modifier = Modifier
                            .testTag("jump_button")
                            .size(68.dp)
                            .background(LibertyAmber, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Jump", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        // Vehicle Spawner Bottom Sheet / Dialog
        if (showSpawnSheet) {
            AlertDialog(
                onDismissRequest = { showSpawnSheet = false },
                title = { Text("Spawn Vehicle", fontWeight = FontWeight.Bold, color = LibertyAmber) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (type in VehicleType.values()) {
                            Button(
                                onClick = {
                                    onSpawnVehicle(type)
                                    showSpawnSheet = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(type.displayName, color = Color.White)
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(type.defaultColor, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpawnSheet = false }) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                },
                containerColor = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 45f

    Box(
        modifier = modifier
            .size(110.dp)
            .background(Color(0x770F172A), CircleShape)
            .border(2.dp, Color(0x5594A3B8), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val distance = newOffset.getDistance()
                        knobOffset = if (distance > maxRadius) {
                            newOffset * (maxRadius / distance)
                        } else {
                            newOffset
                        }
                        onMove(knobOffset.x / maxRadius, knobOffset.y / maxRadius)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(44.dp)
                .background(LibertyAmber, CircleShape)
                .border(2.dp, Color(0xFFFEF3C7), CircleShape)
        )
    }
}

@Composable
fun TouchHoldButton(
    text: String,
    onHoldChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xBB1E293B),
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, Color(0x55FFFFFF), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldChange(true)
                        tryAwaitRelease()
                        onHoldChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}
