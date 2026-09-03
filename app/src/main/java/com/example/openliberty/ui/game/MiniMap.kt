package com.example.openliberty.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.openliberty.model.VehicleInstance
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MiniMap(
    playerX: Float,
    playerY: Float,
    playerHeading: Float,
    vehicles: List<VehicleInstance>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(Color(0xCC0A0F1D))
            .border(2.dp, Color(0xFF64748B), CircleShape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val scale = 1.3f // Pixels per world meter

            // Draw major roads on radar
            val roadColor = Color(0xFF334155)
            val roadWidth = 8f

            // Main arterial cross roads
            for (offsetCoord in listOf(-60f, -30f, 0f, 30f, 60f)) {
                // Vertical avenues
                val worldX = offsetCoord
                val screenX = center.x + (worldX - playerX) * scale
                if (screenX in 0f..size.width) {
                    drawLine(
                        color = roadColor,
                        start = Offset(screenX, 0f),
                        end = Offset(screenX, size.height),
                        strokeWidth = roadWidth
                    )
                }

                // Horizontal streets
                val worldY = offsetCoord
                val screenY = center.y + (worldY - playerY) * scale
                if (screenY in 0f..size.height) {
                    drawLine(
                        color = roadColor,
                        start = Offset(0f, screenY),
                        end = Offset(size.width, screenY),
                        strokeWidth = roadWidth
                    )
                }
            }

            // Draw other vehicles as colored squares
            for (veh in vehicles) {
                val vx = center.x + (veh.x - playerX) * scale
                val vy = center.y + (veh.y - playerY) * scale
                if (vx in 0f..size.width && vy in 0f..size.height) {
                    drawRect(
                        color = veh.color,
                        topLeft = Offset(vx - 3f, vy - 3f),
                        size = androidx.compose.ui.geometry.Size(6f, 6f)
                    )
                }
            }

            // Draw player as pointing triangle at radar center
            val playerPath = Path().apply {
                val tipX = center.x + sin(playerHeading) * 10f
                val tipY = center.y - cos(playerHeading) * 10f

                val leftX = center.x + sin(playerHeading + 2.5f) * 8f
                val leftY = center.y - cos(playerHeading + 2.5f) * 8f

                val rightX = center.x + sin(playerHeading - 2.5f) * 8f
                val rightY = center.y - cos(playerHeading - 2.5f) * 8f

                moveTo(tipX, tipY)
                lineTo(leftX, leftY)
                lineTo(center.x, center.y)
                lineTo(rightX, rightY)
                close()
            }
            drawPath(playerPath, Color(0xFFF59E0B))

            // North indicator
            val northX = center.x
            val northY = 10f
            drawCircle(Color(0xFFEF4444), radius = 3f, center = Offset(northX, northY))
        }
    }
}
