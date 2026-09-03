package com.example.openliberty.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.engine3d.Mat4
import com.example.openliberty.engine3d.Models3D
import com.example.openliberty.engine3d.Poly3D
import com.example.openliberty.engine3d.Vec3
import com.example.openliberty.model.PlayerState
import com.example.openliberty.model.VehicleInstance
import com.example.openliberty.model.VehicleType
import com.example.openliberty.viewmodel.CameraMode
import kotlin.math.*

data class Building3DDef(
    val x: Float,
    val z: Float,
    val w: Float,
    val l: Float,
    val h: Float,
    val wallColor: Color,
    val roofColor: Color,
    val label: String
)

val CITY_BUILDINGS = listOf(
    Building3DDef(-28f, -28f, 18f, 18f, 22f, Color(0xFF78350F), Color(0xFF451A03), "RED LIGHT DISTRICT"),
    Building3DDef(28f, -28f, 22f, 20f, 32f, Color(0xFF1E293B), Color(0xFF0F172A), "LIBERTY BANK"),
    Building3DDef(-28f, 28f, 20f, 22f, 16f, Color(0xFF334155), Color(0xFF1E293B), "8-BALL BOMBS"),
    Building3DDef(28f, 28f, 24f, 24f, 20f, Color(0xFF475569), Color(0xFF1E283D), "PAY 'N' SPRAY"),
    Building3DDef(-70f, -28f, 26f, 24f, 14f, Color(0xFF52525B), Color(0xFF27272A), "PORTLAND HARBOR"),
    Building3DDef(70f, -28f, 24f, 24f, 36f, Color(0xFF1E1B4B), Color(0xFF0F172A), "AMMU-NATION"),
    Building3DDef(-70f, 28f, 22f, 24f, 18f, Color(0xFF701A75), Color(0xFF4A044E), "LUIGI'S CLUB"),
    Building3DDef(70f, 28f, 28f, 28f, 44f, Color(0xFF164E63), Color(0xFF083344), "TORRINGTON TOWER")
)

val STREETLAMPS = listOf(
    Pair(-11f, -45f), Pair(-11f, -15f), Pair(-11f, 15f), Pair(-11f, 45f),
    Pair(11f, -45f), Pair(11f, -15f), Pair(11f, 15f), Pair(11f, 45f),
    Pair(-45f, -11f), Pair(-15f, -11f), Pair(15f, -11f), Pair(45f, -11f),
    Pair(-45f, 11f), Pair(-15f, 11f), Pair(15f, 11f), Pair(45f, 11f)
)

data class ProjectedPoly(
    val points: List<Offset>,
    val color: Color,
    val depth: Float,
    val strokeColor: Color? = null,
    val isEmissive: Boolean = false
)

@Composable
fun World3DRenderer(
    player: PlayerState,
    vehicles: List<VehicleInstance>,
    currentVehicleId: String?,
    isNight: Boolean,
    timeOfDayHours: Float,
    cameraMode: CameraMode,
    orbitYaw: Float = 0f,
    orbitPitch: Float = 14f,
    onOrbitDrag: ((Float, Float) -> Unit)? = null,
    onResetOrbit: (() -> Unit)? = null,
    onCycleCamera: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeVehicle = vehicles.find { it.id == currentVehicleId }
    val isDriving = player.isInVehicle && activeVehicle != null

    // Target focus point in 3D world space
    val targetX = if (isDriving) activeVehicle!!.x else player.x
    val targetY = if (isDriving) 0.65f else (player.z + 0.9f)
    val targetZ = if (isDriving) activeVehicle!!.y else player.y
    val heading = if (isDriving) activeVehicle!!.heading else player.heading
    val speed = if (isDriving) activeVehicle!!.speed else 0f

    // Calculate camera eye position and look-at target based on CameraMode
    val (eye, lookAt) = remember(targetX, targetY, targetZ, heading, speed, cameraMode, orbitYaw, orbitPitch) {
        calculateCamera(
            targetX, targetY, targetZ,
            heading, speed,
            cameraMode, orbitYaw, orbitPitch,
            isDriving
        )
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(cameraMode) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onOrbitDrag?.invoke(dragAmount.x, dragAmount.y)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f

            // 1. Draw 3D Atmospheric Sky & Horizon
            drawAtmosphericSky(
                eye = eye,
                lookAt = lookAt,
                cx = cx,
                cy = cy,
                height = height,
                width = width,
                isNight = isNight,
                timeOfDayHours = timeOfDayHours
            )

            // 2. Camera View Matrix and Perspective Projection parameters
            val up = Vec3(0f, 1f, 0f)
            val viewMat = Mat4.lookAt(eye, lookAt, up)

            val fovY = 64f * (PI.toFloat() / 180f)
            val focalLength = (height * 0.5f) / tan(fovY * 0.5f)

            // 3. Assemble all 3D scene geometry
            val scenePolys = mutableListOf<Poly3D>()

            // Ground plane & Roads
            scenePolys.addAll(generateGroundAndRoads(eye, isNight))

            // Dynamic Headlight Cones projected on road
            if (isDriving && activeVehicle!!.headlightsOn) {
                scenePolys.addAll(generateHeadlightBeams(activeVehicle))
            }

            // Buildings
            for (b in CITY_BUILDINGS) {
                // Distance culling to keep 60 FPS
                val distSq = (b.x - eye.x) * (b.x - eye.x) + (b.z - eye.z) * (b.z - eye.z)
                if (distSq < 22000f) {
                    scenePolys.addAll(
                        Models3D.createBuildingPolygons(
                            b.x, b.z, b.w, b.l, b.h,
                            b.wallColor, b.roofColor, b.label,
                            isNight
                        )
                    )
                }
            }

            // Streetlamps
            for (lamp in STREETLAMPS) {
                val distSq = (lamp.first - eye.x) * (lamp.first - eye.x) + (lamp.second - eye.z) * (lamp.second - eye.z)
                if (distSq < 10000f) {
                    scenePolys.addAll(Models3D.createStreetlampPolygons(lamp.first, lamp.second, isNight))
                }
            }

            // Vehicles in 3D
            for (veh in vehicles) {
                val distSq = (veh.x - eye.x) * (veh.x - eye.x) + (veh.y - eye.z) * (veh.y - eye.z)
                if (distSq < 15000f) {
                    scenePolys.addAll(Models3D.createVehiclePolygons(veh, isNight))
                }
            }

            // Player character on foot in 3D
            if (!player.isInVehicle) {
                scenePolys.addAll(Models3D.createPlayerPolygons(player))
            }

            // 4. Directional Lighting setup (Sun / Moon vector)
            val sunLightDir = if (isNight) {
                Vec3(0.3f, 0.8f, 0.5f).normalize()
            } else {
                Vec3(0.6f, 0.85f, -0.4f).normalize()
            }
            val ambientFactor = if (isNight) 0.32f else 0.48f

            // 5. Transform, Backface-Cull, Clip, Project, and Depth-Sort
            val projectedList = ArrayList<ProjectedPoly>(scenePolys.size)

            for (poly in scenePolys) {
                val verts = poly.vertices
                if (verts.size < 3) continue

                // Transform vertices to camera space
                var allInFront = true
                var avgZ = 0f
                val camVerts = ArrayList<Vec3>(verts.size)
                for (v in verts) {
                    val cv = viewMat.transform(v)
                    camVerts.add(cv)
                    if (cv.z <= 0.35f) {
                        allInFront = false
                    }
                    avgZ += cv.z
                }

                if (!allInFront || camVerts.isEmpty()) continue
                avgZ /= camVerts.size

                // Backface Culling
                if (!poly.isDoubleSided && !poly.isEmissive) {
                    val normal = poly.calculateNormal()
                    val toCamera = (eye - verts[0]).normalize()
                    if (normal.dot(toCamera) <= -0.05f) {
                        continue // Facing away from camera, discard
                    }
                }

                // Shading with Directional Light
                val finalColor = if (poly.isEmissive) {
                    poly.baseColor
                } else {
                    val normal = poly.calculateNormal()
                    val diffuse = max(0f, normal.dot(sunLightDir))
                    val lightMult = (ambientFactor + (1f - ambientFactor) * diffuse).coerceIn(0.18f, 1.0f)
                    Color(
                        red = (poly.baseColor.red * lightMult).coerceIn(0f, 1f),
                        green = (poly.baseColor.green * lightMult).coerceIn(0f, 1f),
                        blue = (poly.baseColor.blue * lightMult).coerceIn(0f, 1f),
                        alpha = poly.baseColor.alpha
                    )
                }

                // Perspective projection to screen
                val screenPoints = ArrayList<Offset>(camVerts.size)
                var hasValidPoint = false
                for (cv in camVerts) {
                    val scale = focalLength / cv.z
                    val sx = cx + cv.x * scale
                    val sy = cy - cv.y * scale
                    screenPoints.add(Offset(sx, sy))
                    if (sx in -100f..(width + 100f) && sy in -100f..(height + 100f)) {
                        hasValidPoint = true
                    }
                }

                if (hasValidPoint) {
                    projectedList.add(
                        ProjectedPoly(
                            points = screenPoints,
                            color = finalColor,
                            depth = avgZ,
                            strokeColor = poly.strokeColor,
                            isEmissive = poly.isEmissive
                        )
                    )
                }
            }

            // 6. Depth sort polygons (furthest to closest: Painter's Algorithm)
            projectedList.sortByDescending { it.depth }

            // 7. Rasterize all 3D Polygons onto Canvas
            val polyPath = Path()
            for (p in projectedList) {
                if (p.points.size < 3) continue
                polyPath.reset()
                polyPath.moveTo(p.points[0].x, p.points[0].y)
                for (i in 1 until p.points.size) {
                    polyPath.lineTo(p.points[i].x, p.points[i].y)
                }
                polyPath.close()

                drawPath(polyPath, color = p.color)

                if (p.strokeColor != null) {
                    drawPath(polyPath, color = p.strokeColor, style = Stroke(width = 1.2f))
                }
            }
        }

        // Top 3D Camera Controls & Feedback
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 10.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Camera Mode Badge & Switcher
            FilledTonalButton(
                onClick = { onCycleCamera?.invoke() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xCC0F172A),
                    contentColor = Color(0xFF38BDF8)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = "Camera Mode",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = cameraMode.displayName,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            // Reset Camera button (if orbit modified)
            if (abs(orbitYaw) > 0.05f || abs(orbitPitch - 14f) > 1f) {
                IconButton(
                    onClick = { onResetOrbit?.invoke() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xCC0F172A),
                        contentColor = Color(0xFFFDE047)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.CenterFocusStrong,
                        contentDescription = "Reset 3D Cam",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Calculates camera Eye position and LookAt position in true 3D space.
 */
private fun calculateCamera(
    targetX: Float, targetY: Float, targetZ: Float,
    heading: Float, speed: Float,
    cameraMode: CameraMode,
    orbitYaw: Float, orbitPitch: Float,
    isDriving: Boolean
): Pair<Vec3, Vec3> {
    val pitchRad = (orbitPitch * PI.toFloat() / 180f).coerceIn(0.04f, 1.4f)
    val totalYaw = heading + orbitYaw

    return when (cameraMode) {
        CameraMode.CHASE_CAM -> {
            // 3D Chase camera trailing behind vehicle/character with speed elasticity
            val baseDist = if (isDriving) 6.4f else 4.2f
            val dynamicDist = baseDist + (abs(speed) * 0.08f).coerceAtMost(2.5f)
            val camHeight = (if (isDriving) 2.4f else 1.8f) + sin(pitchRad) * dynamicDist

            val eyeX = targetX - sin(totalYaw) * cos(pitchRad) * dynamicDist
            val eyeY = targetY + camHeight
            val eyeZ = targetZ + cos(totalYaw) * cos(pitchRad) * dynamicDist

            val lookTarget = Vec3(
                targetX + sin(heading) * 3.5f,
                targetY + 0.3f,
                targetZ - cos(heading) * 3.5f
            )
            Pair(Vec3(eyeX, eyeY, eyeZ), lookTarget)
        }

        CameraMode.HOOD_CAM -> {
            // 3D First Person / Hood Driving Cam
            val hoodForward = if (isDriving) 1.2f else 0.4f
            val eyeX = targetX + sin(heading) * hoodForward
            val eyeY = targetY + (if (isDriving) 0.65f else 0.85f)
            val eyeZ = targetZ - cos(heading) * hoodForward

            val lookTarget = Vec3(
                targetX + sin(totalYaw) * 25f,
                eyeY - sin(pitchRad - 0.2f) * 10f,
                targetZ - cos(totalYaw) * 25f
            )
            Pair(Vec3(eyeX, eyeY, eyeZ), lookTarget)
        }

        CameraMode.ORBIT_CAM -> {
            // 360° Free Orbit Cam around vehicle or player
            val orbitDist = 6.8f
            val eyeX = targetX - sin(orbitYaw) * cos(pitchRad) * orbitDist
            val eyeY = targetY + sin(pitchRad) * orbitDist + 0.8f
            val eyeZ = targetZ + cos(orbitYaw) * cos(pitchRad) * orbitDist

            val lookTarget = Vec3(targetX, targetY + 0.5f, targetZ)
            Pair(Vec3(eyeX, eyeY, eyeZ), lookTarget)
        }

        CameraMode.ISOMETRIC_CAM -> {
            // 3D Isometric high angled tactical view
            val dist = 18f
            val eyeX = targetX - dist * 0.7f
            val eyeY = targetY + dist * 0.8f
            val eyeZ = targetZ + dist * 0.7f
            val lookTarget = Vec3(targetX, targetY, targetZ)
            Pair(Vec3(eyeX, eyeY, eyeZ), lookTarget)
        }

        CameraMode.FREE_FLY -> {
            // 3D High altitude helicopter / bird view
            val eyeX = targetX
            val eyeY = targetY + 28f
            val eyeZ = targetZ + 12f
            val lookTarget = Vec3(targetX, 0f, targetZ)
            Pair(Vec3(eyeX, eyeY, eyeZ), lookTarget)
        }
    }
}

/**
 * Renders atmospheric 3D sky dome, sun/moon, and distant city horizon silhouette.
 */
private fun DrawScope.drawAtmosphericSky(
    eye: Vec3,
    lookAt: Vec3,
    cx: Float,
    cy: Float,
    height: Float,
    width: Float,
    isNight: Boolean,
    timeOfDayHours: Float
) {
    // Determine horizon line Y based on camera pitch
    val forward = (lookAt - eye).normalize()
    val pitch = asin(forward.y.coerceIn(-0.99f, 0.99f))
    val horizonY = (cy + pitch * (height * 0.8f)).coerceIn(height * 0.1f, height * 0.85f)

    // Dynamic sky colors
    val (skyTopColor, skyHorizonColor, groundColor) = when {
        isNight -> Triple(Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF0B0F19))
        timeOfDayHours in 17.5f..20.0f -> Triple(Color(0xFF31103F), Color(0xFFF97316), Color(0xFF18181B)) // Sunset
        timeOfDayHours in 5.5f..7.5f -> Triple(Color(0xFF1E3A8A), Color(0xFFFDE047), Color(0xFF18181B)) // Sunrise
        else -> Triple(Color(0xFF0284C7), Color(0xFF93C5FD), Color(0xFF1E293B)) // Afternoon daylight
    }

    // Sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(skyTopColor, skyHorizonColor),
            startY = 0f,
            endY = horizonY
        ),
        topLeft = Offset(0f, 0f),
        size = androidx.compose.ui.geometry.Size(width, horizonY)
    )

    // Ground base below horizon
    drawRect(
        color = groundColor,
        topLeft = Offset(0f, horizonY),
        size = androidx.compose.ui.geometry.Size(width, height - horizonY)
    )

    // Sun or Moon disk
    val celestialY = horizonY * 0.35f
    val celestialX = cx + cos(timeOfDayHours * 0.26f) * (width * 0.35f)
    val celestialColor = if (isNight) Color(0xFFFEF3C7) else Color(0xFFFEF08A)
    drawCircle(
        color = celestialColor,
        radius = if (isNight) 16f else 28f,
        center = Offset(celestialX, celestialY)
    )
}

/**
 * Builds 3D ground asphalt, North-South avenue, East-West avenue, sidewalks, and markings.
 */
private fun generateGroundAndRoads(eye: Vec3, isNight: Boolean): List<Poly3D> {
    val polys = mutableListOf<Poly3D>()
    val roadAsphalt = Color(0xFF1E293B)
    val sidewalkColor = Color(0xFF475569)
    val yellowLine = Color(0xFFFACC15)
    val whiteLine = Color(0xFFF8FAFC)

    val halfWidth = 8f // half road width (16m wide roads)
    val extent = 110f // grid boundary

    // 1. North-South Road (Z axis)
    polys.add(
        Poly3D(
            listOf(
                Vec3(-halfWidth, 0.01f, -extent),
                Vec3(halfWidth, 0.01f, -extent),
                Vec3(halfWidth, 0.01f, extent),
                Vec3(-halfWidth, 0.01f, extent)
            ),
            roadAsphalt
        )
    )

    // Double Yellow Centerline along North-South Road
    polys.add(
        Poly3D(
            listOf(
                Vec3(-0.16f, 0.02f, -extent),
                Vec3(-0.04f, 0.02f, -extent),
                Vec3(-0.04f, 0.02f, extent),
                Vec3(-0.16f, 0.02f, extent)
            ),
            yellowLine
        )
    )
    polys.add(
        Poly3D(
            listOf(
                Vec3(0.04f, 0.02f, -extent),
                Vec3(0.16f, 0.02f, -extent),
                Vec3(0.16f, 0.02f, extent),
                Vec3(0.04f, 0.02f, extent)
            ),
            yellowLine
        )
    )

    // White dashed lane dividers
    val dashStep = 10f
    var z = -extent
    while (z < extent) {
        // Left lane dash
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-4.0f, 0.02f, z),
                    Vec3(-3.85f, 0.02f, z),
                    Vec3(-3.85f, 0.02f, z + 4.5f),
                    Vec3(-4.0f, 0.02f, z + 4.5f)
                ),
                whiteLine
            )
        )
        // Right lane dash
        polys.add(
            Poly3D(
                listOf(
                    Vec3(3.85f, 0.02f, z),
                    Vec3(4.0f, 0.02f, z),
                    Vec3(4.0f, 0.02f, z + 4.5f),
                    Vec3(3.85f, 0.02f, z + 4.5f)
                ),
                whiteLine
            )
        )
        z += dashStep
    }

    // 2. East-West Road (X axis)
    polys.add(
        Poly3D(
            listOf(
                Vec3(-extent, 0.01f, -halfWidth),
                Vec3(extent, 0.01f, -halfWidth),
                Vec3(extent, 0.01f, halfWidth),
                Vec3(-extent, 0.01f, halfWidth)
            ),
            roadAsphalt
        )
    )
    // Double Yellow Centerline along East-West Road
    polys.add(
        Poly3D(
            listOf(
                Vec3(-extent, 0.02f, -0.16f),
                Vec3(extent, 0.02f, -0.16f),
                Vec3(extent, 0.02f, -0.04f),
                Vec3(-extent, 0.02f, -0.04f)
            ),
            yellowLine
        )
    )
    polys.add(
        Poly3D(
            listOf(
                Vec3(-extent, 0.02f, 0.04f),
                Vec3(extent, 0.02f, 0.04f),
                Vec3(extent, 0.02f, 0.16f),
                Vec3(-extent, 0.02f, 0.16f)
            ),
            yellowLine
        )
    )

    // 3. Sidewalk Curbs (Raised 0.18m)
    // Top-Left quadrant sidewalk
    polys.add(
        Poly3D(
            listOf(
                Vec3(-extent, 0.18f, -extent),
                Vec3(-halfWidth - 0.1f, 0.18f, -extent),
                Vec3(-halfWidth - 0.1f, 0.18f, -halfWidth - 0.1f),
                Vec3(-extent, 0.18f, -halfWidth - 0.1f)
            ),
            sidewalkColor
        )
    )
    // Top-Right quadrant sidewalk
    polys.add(
        Poly3D(
            listOf(
                Vec3(halfWidth + 0.1f, 0.18f, -extent),
                Vec3(extent, 0.18f, -extent),
                Vec3(extent, 0.18f, -halfWidth - 0.1f),
                Vec3(halfWidth + 0.1f, 0.18f, -halfWidth - 0.1f)
            ),
            sidewalkColor
        )
    )
    // Bottom-Left quadrant sidewalk
    polys.add(
        Poly3D(
            listOf(
                Vec3(-extent, 0.18f, halfWidth + 0.1f),
                Vec3(-halfWidth - 0.1f, 0.18f, halfWidth + 0.1f),
                Vec3(-halfWidth - 0.1f, 0.18f, extent),
                Vec3(-extent, 0.18f, extent)
            ),
            sidewalkColor
        )
    )
    // Bottom-Right quadrant sidewalk
    polys.add(
        Poly3D(
            listOf(
                Vec3(halfWidth + 0.1f, 0.18f, halfWidth + 0.1f),
                Vec3(extent, 0.18f, halfWidth + 0.1f),
                Vec3(extent, 0.18f, extent),
                Vec3(halfWidth + 0.1f, 0.18f, extent)
            ),
            sidewalkColor
        )
    )

    return polys
}

/**
 * Projects 3D volumetric light beams onto the asphalt in front of the vehicle headlights.
 */
private fun generateHeadlightBeams(vehicle: VehicleInstance): List<Poly3D> {
    val polys = mutableListOf<Poly3D>()
    val hw = vehicle.type.width / 2f
    val hl = vehicle.type.length / 2f
    val beamLength = 16f
    val beamSpread = 4.2f

    val lightGlowColor = Color(0x35FEF08A)

    // Local light cone vertices on ground
    val leftBeam = listOf(
        Vec3(-hw * 0.7f, 0.03f, hl),
        Vec3(-hw * 0.3f, 0.03f, hl),
        Vec3(-hw * 0.3f - beamSpread * 0.4f, 0.03f, hl + beamLength),
        Vec3(-hw * 0.7f - beamSpread, 0.03f, hl + beamLength)
    )
    val rightBeam = listOf(
        Vec3(hw * 0.3f, 0.03f, hl),
        Vec3(hw * 0.7f, 0.03f, hl),
        Vec3(hw * 0.7f + beamSpread, 0.03f, hl + beamLength),
        Vec3(hw * 0.3f + beamSpread * 0.4f, 0.03f, hl + beamLength)
    )

    // Transform by vehicle world matrix
    val trans = Mat4.translation(vehicle.x, 0.05f, vehicle.y)
    val rotY = Mat4.rotationY(vehicle.heading)
    val mat = trans.multiply(rotY)

    polys.add(Poly3D(leftBeam.map { mat.transform(it) }, lightGlowColor, isEmissive = true))
    polys.add(Poly3D(rightBeam.map { mat.transform(it) }, lightGlowColor, isEmissive = true))

    return polys
}
