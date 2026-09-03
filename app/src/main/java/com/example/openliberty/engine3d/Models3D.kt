package com.example.openliberty.engine3d

import androidx.compose.ui.graphics.Color
import com.example.openliberty.model.PlayerState
import com.example.openliberty.model.VehicleInstance
import com.example.openliberty.model.VehicleType
import kotlin.math.*

object Models3D {

    /**
     * Builds full 3D geometry of a vehicle with body, cabin, windshield, lights, and 4 wheels.
     */
    fun createVehiclePolygons(
        vehicle: VehicleInstance,
        isNight: Boolean
    ): List<Poly3D> {
        val polys = mutableListOf<Poly3D>()

        val hw = vehicle.type.width / 2f
        val hl = vehicle.type.length / 2f
        val hh = 0.55f // chassis height
        val roofH = 0.95f // cabin height
        val carColor = vehicle.color

        // Shaded colors for body parts
        val roofColor = if (vehicle.type == VehicleType.POLICE) Color(0xFFF1F5F9) else carColor.copy(alpha = 0.92f)
        val glassColor = if (vehicle.cabinLightOn) Color(0xFFFDE047) else Color(0xDD1E293B)
        val underbodyColor = Color(0xFF0F172A)
        val bumperColor = Color(0xFF1E283D)

        // Local 3D vertices of vehicle chassis:
        // X = width (left - / right +), Y = height (0 is bottom of chassis), Z = length (front + / rear -)
        val hoodLen = hl * 0.45f
        val trunkLen = hl * 0.35f
        val cabinHalfL = hl - hoodLen - trunkLen

        // 1. Hood (top face, front slanted face)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.9f, hh, hoodLen),
                    Vec3(hw * 0.9f, hh, hoodLen),
                    Vec3(hw * 0.85f, hh * 0.7f, hl),
                    Vec3(-hw * 0.85f, hh * 0.7f, hl)
                ),
                carColor
            )
        )

        // 2. Front Grill & Bumper
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.85f, hh * 0.7f, hl),
                    Vec3(hw * 0.85f, hh * 0.7f, hl),
                    Vec3(hw * 0.85f, 0.15f, hl),
                    Vec3(-hw * 0.85f, 0.15f, hl)
                ),
                bumperColor
            )
        )

        // Front Headlights in 3D
        val lightColor = if (vehicle.headlightsOn) Color(0xFFFEF08A) else Color(0xFFE2E8F0)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.8f, hh * 0.65f, hl + 0.01f),
                    Vec3(-hw * 0.45f, hh * 0.65f, hl + 0.01f),
                    Vec3(-hw * 0.45f, hh * 0.3f, hl + 0.01f),
                    Vec3(-hw * 0.8f, hh * 0.3f, hl + 0.01f)
                ),
                lightColor,
                isEmissive = vehicle.headlightsOn
            )
        )
        polys.add(
            Poly3D(
                listOf(
                    Vec3(hw * 0.45f, hh * 0.65f, hl + 0.01f),
                    Vec3(hw * 0.8f, hh * 0.65f, hl + 0.01f),
                    Vec3(hw * 0.8f, hh * 0.3f, hl + 0.01f),
                    Vec3(hw * 0.45f, hh * 0.3f, hl + 0.01f)
                ),
                lightColor,
                isEmissive = vehicle.headlightsOn
            )
        )

        // 3. Windshield (angled forward)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.8f, roofH, cabinHalfL),
                    Vec3(hw * 0.8f, roofH, cabinHalfL),
                    Vec3(hw * 0.9f, hh, hoodLen),
                    Vec3(-hw * 0.9f, hh, hoodLen)
                ),
                glassColor
            )
        )

        // 4. Cabin Roof
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.75f, roofH, -cabinHalfL),
                    Vec3(hw * 0.75f, roofH, -cabinHalfL),
                    Vec3(hw * 0.8f, roofH, cabinHalfL),
                    Vec3(-hw * 0.8f, roofH, cabinHalfL)
                ),
                roofColor
            )
        )

        // Police Siren Lightbar in 3D
        if (vehicle.type == VehicleType.POLICE) {
            val sirenBlue = if (vehicle.sirenOn && (System.currentTimeMillis() / 250) % 2 == 0L) Color(0xFF38BDF8) else Color(0xFF0369A1)
            val sirenRed = if (vehicle.sirenOn && (System.currentTimeMillis() / 250) % 2 != 0L) Color(0xFFEF4444) else Color(0xFFB91C1C)
            // Lightbar Red box
            polys.add(
                Poly3D(
                    listOf(
                        Vec3(-hw * 0.45f, roofH + 0.12f, 0.1f),
                        Vec3(-0.02f, roofH + 0.12f, 0.1f),
                        Vec3(-0.02f, roofH + 0.12f, -0.1f),
                        Vec3(-hw * 0.45f, roofH + 0.12f, -0.1f)
                    ),
                    sirenRed,
                    isEmissive = vehicle.sirenOn
                )
            )
            // Lightbar Blue box
            polys.add(
                Poly3D(
                    listOf(
                        Vec3(0.02f, roofH + 0.12f, 0.1f),
                        Vec3(hw * 0.45f, roofH + 0.12f, 0.1f),
                        Vec3(hw * 0.45f, roofH + 0.12f, -0.1f),
                        Vec3(0.02f, roofH + 0.12f, -0.1f)
                    ),
                    sirenBlue,
                    isEmissive = vehicle.sirenOn
                )
            )
        }

        // Taxi Sign in 3D
        if (vehicle.type == VehicleType.TAXI) {
            polys.add(
                Poly3D(
                    listOf(
                        Vec3(-hw * 0.35f, roofH + 0.15f, 0.1f),
                        Vec3(hw * 0.35f, roofH + 0.15f, 0.1f),
                        Vec3(hw * 0.35f, roofH, 0.1f),
                        Vec3(-hw * 0.35f, roofH, 0.1f)
                    ),
                    Color(0xFFFEF08A),
                    isEmissive = true
                )
            )
        }

        // 5. Rear Window (slanted backwards)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.85f, hh, -trunkLen),
                    Vec3(hw * 0.85f, hh, -trunkLen),
                    Vec3(hw * 0.75f, roofH, -cabinHalfL),
                    Vec3(-hw * 0.75f, roofH, -cabinHalfL)
                ),
                glassColor
            )
        )

        // 6. Trunk Deck
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.85f, hh * 0.7f, -hl),
                    Vec3(hw * 0.85f, hh * 0.7f, -hl),
                    Vec3(hw * 0.85f, hh, -trunkLen),
                    Vec3(-hw * 0.85f, hh, -trunkLen)
                ),
                carColor
            )
        )

        // 7. Rear Bumper & Taillights
        val tailColor = if (vehicle.speed < -0.2f || vehicle.isDrifting) Color(0xFFFF1111) else Color(0xFF991B1B)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.85f, 0.15f, -hl),
                    Vec3(hw * 0.85f, 0.15f, -hl),
                    Vec3(hw * 0.85f, hh * 0.7f, -hl),
                    Vec3(-hw * 0.85f, hh * 0.7f, -hl)
                ),
                bumperColor
            )
        )
        // Left & Right Taillight lamps
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.8f, hh * 0.65f, -hl - 0.01f),
                    Vec3(-hw * 0.5f, hh * 0.65f, -hl - 0.01f),
                    Vec3(-hw * 0.5f, hh * 0.35f, -hl - 0.01f),
                    Vec3(-hw * 0.8f, hh * 0.35f, -hl - 0.01f)
                ),
                tailColor,
                isEmissive = true
            )
        )
        polys.add(
            Poly3D(
                listOf(
                    Vec3(hw * 0.5f, hh * 0.65f, -hl - 0.01f),
                    Vec3(hw * 0.8f, hh * 0.65f, -hl - 0.01f),
                    Vec3(hw * 0.8f, hh * 0.35f, -hl - 0.01f),
                    Vec3(hw * 0.5f, hh * 0.35f, -hl - 0.01f)
                ),
                tailColor,
                isEmissive = true
            )
        )

        // 8. Left and Right Body Side Panels
        // Left side
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw, 0.15f, -hl),
                    Vec3(-hw, 0.15f, hl),
                    Vec3(-hw, hh, hl * 0.85f),
                    Vec3(-hw, hh, -hl * 0.85f)
                ),
                carColor
            )
        )
        // Right side
        polys.add(
            Poly3D(
                listOf(
                    Vec3(hw, 0.15f, hl),
                    Vec3(hw, 0.15f, -hl),
                    Vec3(hw, hh, -hl * 0.85f),
                    Vec3(hw, hh, hl * 0.85f)
                ),
                carColor
            )
        )

        // 9. Side Cabin Windows (Left & Right)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-hw * 0.92f, hh, -trunkLen),
                    Vec3(-hw * 0.92f, hh, hoodLen),
                    Vec3(-hw * 0.78f, roofH, cabinHalfL),
                    Vec3(-hw * 0.78f, roofH, -cabinHalfL)
                ),
                glassColor
            )
        )
        polys.add(
            Poly3D(
                listOf(
                    Vec3(hw * 0.92f, hh, hoodLen),
                    Vec3(hw * 0.92f, hh, -trunkLen),
                    Vec3(hw * 0.78f, roofH, -cabinHalfL),
                    Vec3(hw * 0.78f, roofH, cabinHalfL)
                ),
                glassColor
            )
        )

        // 10. 4 Separate 3D Wheels with Rim, Tire, Steering & Spin
        val wheelRadius = 0.34f
        val wheelWidth = 0.22f
        val wheelBaseZ = hl * 0.58f
        val wheelTrackX = hw * 0.92f

        // Front Left Wheel (steered)
        polys.addAll(create3DWheel(-wheelTrackX, wheelRadius, wheelBaseZ, wheelRadius, wheelWidth, vehicle.steeringAngle, vehicle.wheelAngle, isLeft = true))
        // Front Right Wheel (steered)
        polys.addAll(create3DWheel(wheelTrackX, wheelRadius, wheelBaseZ, wheelRadius, wheelWidth, vehicle.steeringAngle, vehicle.wheelAngle, isLeft = false))
        // Rear Left Wheel (fixed steer)
        polys.addAll(create3DWheel(-wheelTrackX, wheelRadius, -wheelBaseZ, wheelRadius, wheelWidth, 0f, vehicle.wheelAngle, isLeft = true))
        // Rear Right Wheel (fixed steer)
        polys.addAll(create3DWheel(wheelTrackX, wheelRadius, -wheelBaseZ, wheelRadius, wheelWidth, 0f, vehicle.wheelAngle, isLeft = false))

        // Transform all vehicle local polygons into World Space using vehicle position, heading, pitch & roll!
        val worldMat = buildVehicleWorldMatrix(vehicle)
        return polys.map { poly ->
            val worldVerts = poly.vertices.map { v -> worldMat.transform(v) }
            poly.copy(vertices = worldVerts)
        }
    }

    private fun create3DWheel(
        cx: Float, cy: Float, cz: Float,
        radius: Float, width: Float,
        steerAngle: Float, spinAngle: Float,
        isLeft: Boolean
    ): List<Poly3D> {
        val wheelPolys = mutableListOf<Poly3D>()
        val segments = 8
        val halfW = width / 2f
        val tireColor = Color(0xFF0F172A)
        val rimColor = Color(0xFFCBD5E1)
        val hubColor = Color(0xFF64748B)

        // Steer & spin transformation matrix for wheel
        val rotSteer = Mat4.rotationY(steerAngle)
        val rotSpin = Mat4.rotationX(spinAngle)
        val trans = Mat4.translation(cx, cy, cz)
        val wheelMat = trans.multiply(rotSteer).multiply(rotSpin)

        val outerPoints = mutableListOf<Vec3>()
        val innerPoints = mutableListOf<Vec3>()

        for (i in 0 until segments) {
            val angle = (i.toFloat() / segments) * 2f * PI.toFloat()
            val y = cos(angle) * radius
            val z = sin(angle) * radius
            val outX = if (isLeft) -halfW else halfW
            val inX = if (isLeft) halfW else -halfW
            outerPoints.add(wheelMat.transform(Vec3(outX, y, z)))
            innerPoints.add(wheelMat.transform(Vec3(inX, y, z)))
        }

        // Wheel tread faces
        for (i in 0 until segments) {
            val next = (i + 1) % segments
            wheelPolys.add(
                Poly3D(
                    listOf(outerPoints[i], outerPoints[next], innerPoints[next], innerPoints[i]),
                    tireColor
                )
            )
        }

        // Outer wheel cap with rim & hub
        val centerOuter = wheelMat.transform(Vec3(if (isLeft) -halfW else halfW, 0f, 0f))
        for (i in 0 until segments) {
            val next = (i + 1) % segments
            val segColor = if (i % 2 == 0) rimColor else hubColor
            wheelPolys.add(
                Poly3D(
                    listOf(centerOuter, outerPoints[i], outerPoints[next]),
                    segColor
                )
            )
        }

        return wheelPolys
    }

    private fun buildVehicleWorldMatrix(v: VehicleInstance): Mat4 {
        // Translation -> RotationY (heading) -> RotationX (pitch) -> RotationZ (roll)
        val trans = Mat4.translation(v.x, 0.05f, v.y)
        val rotY = Mat4.rotationY(v.heading)
        val rotX = Mat4.rotationX(v.pitch)
        val rotZ = Mat4.rotationZ(v.roll)
        return trans.multiply(rotY).multiply(rotX).multiply(rotZ)
    }

    /**
     * Builds full 3D geometry of the player character (Claude).
     */
    fun createPlayerPolygons(player: PlayerState): List<Poly3D> {
        val polys = mutableListOf<Poly3D>()
        val py = player.z // Jump height
        val phase = player.walkCyclePhase
        val legSwing = if (player.isWalking) sin(phase) * 0.35f else 0f
        val armSwing = if (player.isWalking) -sin(phase) * 0.4f else 0f

        // Player model parts in local space:
        // 1. Torso & Leather Jacket
        val jacketColor = Color(0xFF111827)
        val collarColor = Color(0xFF1E293B)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-0.24f, 0.9f, -0.12f),
                    Vec3(0.24f, 0.9f, -0.12f),
                    Vec3(0.24f, 1.45f, -0.12f),
                    Vec3(-0.24f, 1.45f, -0.12f)
                ),
                jacketColor
            )
        )
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-0.24f, 1.45f, 0.12f),
                    Vec3(0.24f, 1.45f, 0.12f),
                    Vec3(0.24f, 0.9f, 0.12f),
                    Vec3(-0.24f, 0.9f, 0.12f)
                ),
                collarColor
            )
        )

        // 2. Head & Hair
        val skinColor = Color(0xFFFBBF24)
        val hairColor = Color(0xFF451A03)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-0.12f, 1.5f, -0.12f),
                    Vec3(0.12f, 1.5f, -0.12f),
                    Vec3(0.12f, 1.76f, -0.12f),
                    Vec3(-0.12f, 1.76f, -0.12f)
                ),
                hairColor
            )
        )
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-0.12f, 1.76f, 0.12f),
                    Vec3(0.12f, 1.76f, 0.12f),
                    Vec3(0.12f, 1.5f, 0.12f),
                    Vec3(-0.12f, 1.5f, 0.12f)
                ),
                skinColor
            )
        )

        // 3. Legs (Claude's Green Cargo Pants & Boots)
        val cargoColor = Color(0xFF14532D)
        val bootColor = Color(0xFF0F172A)

        // Left Leg
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-0.18f, 0.15f, -0.08f + legSwing),
                    Vec3(-0.04f, 0.15f, -0.08f + legSwing),
                    Vec3(-0.04f, 0.9f, 0f),
                    Vec3(-0.18f, 0.9f, 0f)
                ),
                cargoColor
            )
        )
        // Left Boot
        polys.add(
            Poly3D(
                listOf(
                    Vec3(-0.19f, 0f, -0.12f + legSwing),
                    Vec3(-0.03f, 0f, -0.12f + legSwing),
                    Vec3(-0.03f, 0.15f, -0.08f + legSwing),
                    Vec3(-0.19f, 0.15f, -0.08f + legSwing)
                ),
                bootColor
            )
        )

        // Right Leg
        polys.add(
            Poly3D(
                listOf(
                    Vec3(0.04f, 0.15f, -0.08f - legSwing),
                    Vec3(0.18f, 0.15f, -0.08f - legSwing),
                    Vec3(0.18f, 0.9f, 0f),
                    Vec3(0.04f, 0.9f, 0f)
                ),
                cargoColor
            )
        )
        // Right Boot
        polys.add(
            Poly3D(
                listOf(
                    Vec3(0.03f, 0f, -0.12f - legSwing),
                    Vec3(0.19f, 0f, -0.12f - legSwing),
                    Vec3(0.19f, 0.15f, -0.08f - legSwing),
                    Vec3(0.03f, 0.15f, -0.08f - legSwing)
                ),
                bootColor
            )
        )

        // Transform player polygons by heading and world position
        val trans = Mat4.translation(player.x, py, player.y)
        val rotY = Mat4.rotationY(player.heading)
        val mat = trans.multiply(rotY)

        return polys.map { poly ->
            val worldVerts = poly.vertices.map { v -> mat.transform(v) }
            poly.copy(vertices = worldVerts)
        }
    }

    /**
     * Builds full 3D extruded building with 4 walls, windows, and roof terrace.
     */
    fun createBuildingPolygons(
        bX: Float, bZ: Float, bW: Float, bL: Float, bH: Float,
        wallColor: Color, roofColor: Color, label: String,
        isNight: Boolean
    ): List<Poly3D> {
        val polys = mutableListOf<Poly3D>()
        val hw = bW / 2f
        val hl = bL / 2f
        val minX = bX - hw
        val maxX = bX + hw
        val minZ = bZ - hl
        val maxZ = bZ + hl

        // 1. Front Wall (+Z)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(minX, 0f, maxZ),
                    Vec3(maxX, 0f, maxZ),
                    Vec3(maxX, bH, maxZ),
                    Vec3(minX, bH, maxZ)
                ),
                wallColor
            )
        )

        // 2. Back Wall (-Z)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(maxX, 0f, minZ),
                    Vec3(minX, 0f, minZ),
                    Vec3(minX, bH, minZ),
                    Vec3(maxX, bH, minZ)
                ),
                wallColor.copy(alpha = 0.85f)
            )
        )

        // 3. Left Wall (-X)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(minX, 0f, minZ),
                    Vec3(minX, 0f, maxZ),
                    Vec3(minX, bH, maxZ),
                    Vec3(minX, bH, minZ)
                ),
                wallColor.copy(alpha = 0.9f)
            )
        )

        // 4. Right Wall (+X)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(maxX, 0f, maxZ),
                    Vec3(maxX, 0f, minZ),
                    Vec3(maxX, bH, minZ),
                    Vec3(maxX, bH, maxZ)
                ),
                wallColor.copy(alpha = 0.95f)
            )
        )

        // 5. Roof Slab
        polys.add(
            Poly3D(
                listOf(
                    Vec3(minX, bH, minZ),
                    Vec3(maxX, bH, minZ),
                    Vec3(maxX, bH, maxZ),
                    Vec3(minX, bH, maxZ)
                ),
                roofColor
            )
        )

        // 6. Roof Ledge border
        polys.add(
            Poly3D(
                listOf(
                    Vec3(minX + 0.5f, bH + 0.6f, minZ + 0.5f),
                    Vec3(maxX - 0.5f, bH + 0.6f, minZ + 0.5f),
                    Vec3(maxX - 0.5f, bH + 0.6f, maxZ - 0.5f),
                    Vec3(minX + 0.5f, bH + 0.6f, maxZ - 0.5f)
                ),
                Color(0xFF334155),
                isDoubleSided = true
            )
        )

        // 7. Grid of 3D Window panes on Front Wall
        val cols = (bW / 4f).toInt().coerceIn(2, 6)
        val rows = (bH / 4f).toInt().coerceIn(2, 8)
        val colStep = bW / (cols + 1)
        val rowStep = bH / (rows + 1)

        for (c in 1..cols) {
            for (r in 1..rows) {
                val wx = minX + c * colStep
                val wy = r * rowStep
                val isLit = isNight && ((c + r) % 3 == 0)
                val winColor = if (isLit) Color(0xFFFDE047) else if (isNight) Color(0xFF0F172A) else Color(0xFF67E8F9)
                polys.add(
                    Poly3D(
                        listOf(
                            Vec3(wx - 0.6f, wy - 0.6f, maxZ + 0.05f),
                            Vec3(wx + 0.6f, wy - 0.6f, maxZ + 0.05f),
                            Vec3(wx + 0.6f, wy + 0.6f, maxZ + 0.05f),
                            Vec3(wx - 0.6f, wy + 0.6f, maxZ + 0.05f)
                        ),
                        winColor,
                        isEmissive = isLit
                    )
                )
            }
        }

        return polys
    }

    /**
     * Builds a 3D streetlamp with pole and lantern head.
     */
    fun createStreetlampPolygons(lx: Float, lz: Float, isNight: Boolean): List<Poly3D> {
        val polys = mutableListOf<Poly3D>()
        val poleColor = Color(0xFF334155)
        val lampHeight = 6.0f

        // Vertical Pole
        polys.add(
            Poly3D(
                listOf(
                    Vec3(lx - 0.1f, 0f, lz),
                    Vec3(lx + 0.1f, 0f, lz),
                    Vec3(lx + 0.08f, lampHeight, lz),
                    Vec3(lx - 0.08f, lampHeight, lz)
                ),
                poleColor,
                isDoubleSided = true
            )
        )

        // Horizontal Arm
        polys.add(
            Poly3D(
                listOf(
                    Vec3(lx, lampHeight - 0.1f, lz),
                    Vec3(lx + 1.2f, lampHeight - 0.1f, lz),
                    Vec3(lx + 1.2f, lampHeight + 0.1f, lz),
                    Vec3(lx, lampHeight + 0.1f, lz)
                ),
                poleColor,
                isDoubleSided = true
            )
        )

        // Lantern Head
        val bulbColor = if (isNight) Color(0xFFFEF3C7) else Color(0xFFCBD5E1)
        polys.add(
            Poly3D(
                listOf(
                    Vec3(lx + 1.0f, lampHeight - 0.25f, lz - 0.2f),
                    Vec3(lx + 1.4f, lampHeight - 0.25f, lz - 0.2f),
                    Vec3(lx + 1.4f, lampHeight - 0.25f, lz + 0.2f),
                    Vec3(lx + 1.0f, lampHeight - 0.25f, lz + 0.2f)
                ),
                bulbColor,
                isEmissive = isNight
            )
        )

        return polys
    }

    /**
     * Builds a 3D traffic cone for the test track.
     */
    fun createConePolygons(cx: Float, cz: Float): List<Poly3D> {
        val polys = mutableListOf<Poly3D>()
        val coneColor = Color(0xFFF97316)
        val whiteStripe = Color(0xFFF8FAFC)
        val coneH = 0.8f
        val coneBaseR = 0.35f
        val segments = 6

        // Base square
        polys.add(
            Poly3D(
                listOf(
                    Vec3(cx - 0.4f, 0.02f, cz - 0.4f),
                    Vec3(cx + 0.4f, 0.02f, cz - 0.4f),
                    Vec3(cx + 0.4f, 0.02f, cz + 0.4f),
                    Vec3(cx - 0.4f, 0.02f, cz + 0.4f)
                ),
                Color(0xFF1E293B)
            )
        )

        // Conical sides
        val tip = Vec3(cx, coneH, cz)
        for (i in 0 until segments) {
            val a1 = (i.toFloat() / segments) * 2f * PI.toFloat()
            val a2 = ((i + 1).toFloat() / segments) * 2f * PI.toFloat()
            val b1 = Vec3(cx + cos(a1) * coneBaseR, 0.02f, cz + sin(a1) * coneBaseR)
            val b2 = Vec3(cx + cos(a2) * coneBaseR, 0.02f, cz + sin(a2) * coneBaseR)
            val useColor = if (i % 2 == 0) coneColor else whiteStripe
            polys.add(Poly3D(listOf(tip, b1, b2), useColor))
        }

        return polys
    }
}
