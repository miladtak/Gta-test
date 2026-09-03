package com.example.openliberty.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.ui.theme.LibertyAmber
import com.example.openliberty.ui.theme.LibertyCyan
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF070A10),
                        Color(0xFF020408)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Game Logo / Title Banner
            Surface(
                color = Color(0x33F59E0B),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LibertyAmber.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "LIBERTY CITY 2001",
                    color = LibertyAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "OPENLIBERTY 3D",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "3D Open World & Vehicle Physics Engine",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Menu Buttons
            MenuButton(
                text = "Play Liberty City 3D",
                subtitle = "3D Open world with 3rd-person chase camera & vehicle physics",
                icon = Icons.Default.DirectionsCar,
                primaryColor = LibertyAmber,
                testTag = "play_game_button",
                onClick = { viewModel.navigateTo(AppScreen.OPEN_WORLD) }
            )

            MenuButton(
                text = "3D Vehicle Test Chamber",
                subtitle = "3D track with handling, drifting, slalom cones & lighting",
                icon = Icons.Default.Speed,
                primaryColor = LibertyCyan,
                testTag = "vehicle_test_button",
                onClick = { viewModel.navigateTo(AppScreen.VEHICLE_TEST) }
            )

            MenuButton(
                text = "RenderWare Inspector",
                subtitle = "TXD Texture Viewer, DFF 3D Models, IPL Placements, FlyCam",
                icon = Icons.Default.Category,
                primaryColor = Color(0xFFA855F7),
                testTag = "inspector_button",
                onClick = { viewModel.navigateTo(AppScreen.RENDERWARE_INSPECTOR) }
            )

            MenuButton(
                text = "Options & Settings",
                subtitle = "Audio volumes, Day/Night toggle, Touch Controls mode",
                icon = Icons.Default.Settings,
                primaryColor = Color(0xFF38BDF8),
                testTag = "options_button",
                onClick = { viewModel.navigateTo(AppScreen.OPTIONS) }
            )

            MenuButton(
                text = "Credits & Attribution",
                subtitle = "Original repo, Maaacks Menus, and project contributors",
                icon = Icons.Default.Info,
                primaryColor = Color(0xFF64748B),
                testTag = "credits_button",
                onClick = { viewModel.navigateTo(AppScreen.CREDITS) }
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    subtitle: String,
    icon: ImageVector,
    primaryColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .testTag(testTag),
        color = Color(0xFF0F172A)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8), lineHeight = 14.sp)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF475569))
        }
    }
}
