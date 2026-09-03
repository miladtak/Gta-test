package com.example.openliberty.ui.inspector

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openliberty.data.RenderWareSampleData
import com.example.openliberty.model.RwClumpModel
import com.example.openliberty.model.RwTexture
import com.example.openliberty.ui.theme.LibertyAmber
import com.example.openliberty.ui.theme.LibertyCyan
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderWareInspectorScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("TXD Textures", "DFF 3D Models", "IPL Placements", "FlyCam Test")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("RenderWare Engine Inspector", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("GTA 3 Reverse-Engineered Chunks", fontSize = 11.sp, color = LibertyAmber)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("inspector_back_button")
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
        ) {
            // Tab Selector Row
            PrimaryTabRow(
                selectedTabIndex = state.inspectorTab,
                containerColor = Color(0xFF131926),
                contentColor = LibertyAmber
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.inspectorTab == index,
                        onClick = { viewModel.setInspectorTab(index) },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Tab Content
            when (state.inspectorTab) {
                0 -> TxdViewerTab(
                    selectedIndex = state.selectedTextureIndex,
                    onSelect = viewModel::selectTexture
                )
                1 -> DffModelViewerTab(
                    selectedIndex = state.selectedDffIndex,
                    isWireframe = state.isWireframeEnabled,
                    rotX = state.modelRotX,
                    rotY = state.modelRotY,
                    zoom = state.modelZoom,
                    onSelect = viewModel::selectDffModel,
                    onToggleWireframe = viewModel::toggleWireframe,
                    onRotate = viewModel::rotateModel,
                    onZoom = viewModel::zoomModel
                )
                2 -> IplPlacementTab()
                3 -> FlyCamTab(
                    altitude = state.flyCamAltitude,
                    onPan = viewModel::panFlyCam,
                    onChangeAltitude = viewModel::changeFlyAltitude
                )
            }
        }
    }
}

@Composable
fun TxdViewerTab(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val textures = RenderWareSampleData.textures
    val selected = textures.getOrElse(selectedIndex) { textures.first() }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Texture List
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131926))
        ) {
            Text(
                "TEXTURE DICTIONARY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LibertyAmber,
                modifier = Modifier.padding(12.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(textures) { index, tex ->
                    val isSelected = index == selectedIndex
                    Surface(
                        onClick = { onSelect(index) },
                        color = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(tex.previewColorPrimary, CircleShape)
                            )
                            Column {
                                Text(
                                    tex.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) LibertyAmber else Color.White
                                )
                                Text(
                                    "${tex.width}x${tex.height} • ${tex.format}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Texture Details & Preview
        Card(
            modifier = Modifier
                .weight(1.4f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131926))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(selected.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                // Texture Canvas Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        // Background pattern
                        drawRect(selected.previewColorSecondary)
                        drawRoundRect(
                            selected.previewColorPrimary,
                            topLeft = Offset(15f, 15f),
                            size = Size(size.width - 30f, size.height - 30f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )
                        // Mipmap grid indicator
                        val step = size.width / 4f
                        for (i in 1..3) {
                            drawLine(Color(0x33FFFFFF), Offset(i * step, 0f), Offset(i * step, size.height), strokeWidth = 1f)
                            drawLine(Color(0x33FFFFFF), Offset(0f, i * step), Offset(size.width, i * step), strokeWidth = 1f)
                        }
                    }
                }

                // Metadata details
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DetailRow("Format", selected.format)
                        DetailRow("Resolution", "${selected.width} x ${selected.height}")
                        DetailRow("Bit Depth", "${selected.depth}-bit")
                        DetailRow("Mipmap Levels", "${selected.mipmapCount}")
                        DetailRow("Mask Name", if (selected.maskName.isEmpty()) "None" else selected.maskName)
                    }
                }

                Text(
                    selected.description,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun DffModelViewerTab(
    selectedIndex: Int,
    isWireframe: Boolean,
    rotX: Float,
    rotY: Float,
    zoom: Float,
    onSelect: (Int) -> Unit,
    onToggleWireframe: () -> Unit,
    onRotate: (Float, Float) -> Unit,
    onZoom: (Float) -> Unit
) {
    val models = RenderWareSampleData.dffModels
    val selected = models.getOrElse(selectedIndex) { models.first() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Model Selection Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            models.forEachIndexed { index, m ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    label = { Text(m.name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LibertyAmber,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // 3D Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onRotate(dragAmount.x, dragAmount.y)
                    }
                }
        ) {
            // Interactive 3D Model Rendering
            Dff3DCanvas(
                model = selected,
                rotX = rotX,
                rotY = rotY,
                zoom = zoom,
                isWireframe = isWireframe,
                modifier = Modifier.fillMaxSize()
            )

            // Overlaid controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggleWireframe,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isWireframe) LibertyCyan else Color(0x991E293B)
                    )
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = "Toggle Wireframe", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isWireframe) "Wireframe" else "Shaded", fontSize = 11.sp)
                }

                IconButton(
                    onClick = { onZoom(1.2f) },
                    modifier = Modifier.background(Color(0x991E293B), CircleShape)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                }
                IconButton(
                    onClick = { onZoom(0.8f) },
                    modifier = Modifier.background(Color(0x991E293B), CircleShape)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }
            }

            // Model Stats Pill
            Surface(
                color = Color(0xCC1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Verts: ${selected.vertices.size}", fontSize = 11.sp, color = LibertyAmber)
                    Text("Tris: ${selected.triangles.size}", fontSize = 11.sp, color = LibertyCyan)
                    Text("Frames: ${selected.frameCount}", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun Dff3DCanvas(
    model: RwClumpModel,
    rotX: Float,
    rotY: Float,
    zoom: Float,
    isWireframe: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseScale = (size.minDimension / 4f) * zoom

        val radX = Math.toRadians(rotX.toDouble())
        val radY = Math.toRadians(rotY.toDouble())

        // Transform 3D vertices to 2D screen coordinates
        val screenPoints = model.vertices.map { v ->
            // Rotate Y (Yaw)
            val x1 = v.x * cos(radY) + v.z * sin(radY)
            val y1 = v.y
            val z1 = -v.x * sin(radY) + v.z * cos(radY)

            // Rotate X (Pitch)
            val x2 = x1
            val y2 = y1 * cos(radX) - z1 * sin(radX)
            val z2 = y1 * sin(radX) + z1 * cos(radX)

            // Perspective factor
            val dist = 5.0
            val pFactor = dist / (dist + z2)

            val sx = cx + (x2 * pFactor * baseScale).toFloat()
            val sy = cy - (y2 * pFactor * baseScale).toFloat()
            Offset(sx, sy)
        }

        // Draw triangles
        for (tri in model.triangles) {
            if (tri.a < screenPoints.size && tri.b < screenPoints.size && tri.c < screenPoints.size) {
                val p1 = screenPoints[tri.a]
                val p2 = screenPoints[tri.b]
                val p3 = screenPoints[tri.c]

                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    close()
                }

                val mat = model.materials.getOrNull(tri.materialIndex) ?: model.materials.firstOrNull()
                val matColor = mat?.color ?: Color(0xFF64748B)

                if (!isWireframe) {
                    // Shaded face
                    drawPath(path, matColor.copy(alpha = 0.75f))
                    // Subtle edge outline
                    drawPath(path, matColor.copy(alpha = 0.95f), style = Stroke(width = 1f))
                } else {
                    // Wireframe only
                    drawPath(path, LibertyCyan, style = Stroke(width = 1.2f))
                }
            }
        }

        // Draw vertex points
        for (pt in screenPoints) {
            drawCircle(LibertyAmber, radius = 2f, center = pt)
        }
    }
}

@Composable
fun IplPlacementTab() {
    val placements = RenderWareSampleData.samplePlacements

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("STREAMED ITEM PLACEMENTS (IPL / IDE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LibertyAmber)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(placements.size) { i ->
                val p = placements[i]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131926))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(p.modelName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Instance ID #${p.id}", fontSize = 11.sp, color = LibertyCyan)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Pos: (%.1f, %.1f, %.1f)".format(p.posX, p.posY, p.posZ),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                "LOD: ${p.lodDistance.toInt()}m",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlyCamTab(
    altitude: Float,
    onPan: (Float, Float) -> Unit,
    onChangeAltitude: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("FREE-FLY CAMERA CONTROLLER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LibertyAmber)
        Text("Drag canvas to pan camera across Liberty City coordinate space.", fontSize = 11.sp, color = Color(0xFF94A3B8))

        // Touch Pan Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onPan(-dragAmount.x, dragAmount.y)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Flight, contentDescription = null, tint = LibertyAmber, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Swipe / Drag to Fly", fontSize = 13.sp, color = Color.White)
                Text("Altitude: ${altitude.toInt()}m", fontSize = 11.sp, color = LibertyCyan)
            }
        }

        // Altitude Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Altitude: ${altitude.toInt()}m", color = Color.White, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onChangeAltitude(-5f) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))) {
                    Text("-5m")
                }
                Button(onClick = { onChangeAltitude(5f) }, colors = ButtonDefaults.buttonColors(containerColor = LibertyAmber)) {
                    Text("+5m", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
