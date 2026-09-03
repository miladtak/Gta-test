package com.example.openliberty.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.ui.theme.LibertyAmber
import com.example.openliberty.ui.theme.LibertyCyan
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits & Attribution", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("credits_back_button")
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131926)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("PROJECT OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LibertyAmber)
                    Text("OpenLiberty - GTA 3 Sandbox Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "An open-source sandbox inspired by GTA 3, implementing real-time vehicle physics, lighting cycles, interactive radio stations, player movement, and RenderWare asset inspection tools.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131926)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("ORIGINAL REPOSITORY & AUTHORS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LibertyCyan)
                    Text("Repository: miladtak/Game-gta-for-Godot-", fontSize = 13.sp, color = Color.White)
                    Text("Original Framework: Godot Engine / GDScript", fontSize = 13.sp, color = Color(0xFF94A3B8))
                    Text("Target Framework: Android / Kotlin / Jetpack Compose", fontSize = 13.sp, color = LibertyAmber)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131926)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("THIRD-PARTY TEMPLATES & ASSETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LibertyAmber)
                    Text("• Maaacks Menus Template (Menu, Audio & Video Options, Credits)", fontSize = 12.sp, color = Color.White)
                    Text("• Third-Person-Controller for Character & SpringArm physics", fontSize = 12.sp, color = Color.White)
                    Text("• RenderWare (RW) format reverse-engineering specifications", fontSize = 12.sp, color = Color.White)
                    Text("• Procedural Audio Synthesizer for engine rumble, sirens, and radio beats", fontSize = 12.sp, color = Color.White)
                }
            }

            Button(
                onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LibertyAmber)
            ) {
                Text("Return to Main Menu", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
