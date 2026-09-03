package com.example.openliberty.data

import androidx.compose.ui.graphics.Color
import com.example.openliberty.model.*

object RenderWareSampleData {

    val textures = listOf(
        RwTexture(
            name = "car1_001_COLOR_BASIC",
            maskName = "car1_001_a",
            width = 512,
            height = 512,
            depth = 32,
            format = "DXT3 (Explicit Alpha)",
            mipmapCount = 5,
            previewColorPrimary = Color(0xFFE11D48),
            previewColorSecondary = Color(0xFF1E293B),
            description = "Liberty City sedan livery texture with metallic clearcoat and headlight normals."
        ),
        RwTexture(
            name = "Pickup-Truck_Zombie_Atlas",
            maskName = "pickup_a",
            width = 1024,
            height = 1024,
            depth = 32,
            format = "DXT1 (1-bit Alpha)",
            mipmapCount = 6,
            previewColorPrimary = Color(0xFF2563EB),
            previewColorSecondary = Color(0xFF475569),
            description = "High-resolution vehicle body atlas including cab panels, grill, chrome bumper and tire treads."
        ),
        RwTexture(
            name = "worldgrid_heightmap",
            maskName = "",
            width = 256,
            height = 256,
            depth = 16,
            format = "RGBA8888 Uncompressed",
            mipmapCount = 3,
            previewColorPrimary = Color(0xFF334155),
            previewColorSecondary = Color(0xFF0F172A),
            description = "Terrain collision elevation grid for Portland industrial docks district."
        ),
        RwTexture(
            name = "worldgrid_orm",
            maskName = "",
            width = 512,
            height = 512,
            depth = 32,
            format = "DXT1",
            mipmapCount = 4,
            previewColorPrimary = Color(0xFF0D9488),
            previewColorSecondary = Color(0xFF134E4A),
            description = "Occlusion-Roughness-Metallic PBR composite texture for prototype ground."
        ),
        RwTexture(
            name = "addition_symbol",
            maskName = "add_sym_a",
            width = 64,
            height = 64,
            depth = 32,
            format = "RGBA8888 (32-bit)",
            mipmapCount = 1,
            previewColorPrimary = Color(0xFFF59E0B),
            previewColorSecondary = Color(0xFF78350F),
            description = "Maaacks Menus UI symbol glyph for option increment controls."
        ),
        RwTexture(
            name = "portland_brick_facade",
            maskName = "",
            width = 512,
            height = 512,
            depth = 24,
            format = "DXT1",
            mipmapCount = 4,
            previewColorPrimary = Color(0xFF9A3412),
            previewColorSecondary = Color(0xFF431407),
            description = "Weathered Red Brick facade texture for Red Light District apartments."
        )
    )

    val dffModels: List<RwClumpModel> by lazy {
        listOf(
            createCarClump(),
            createPickupClump(),
            createCharacterClump(),
            createSuzanneClump(),
            createBuildingClump()
        )
    }

    private fun createCarClump(): RwClumpModel {
        val verts = listOf(
            // Bottom chassis
            RwVertex3D(-1.1f, 0.2f, -2.2f),
            RwVertex3D(1.1f, 0.2f, -2.2f),
            RwVertex3D(1.1f, 0.2f, 2.2f),
            RwVertex3D(-1.1f, 0.2f, 2.2f),
            // Hood & trunk level
            RwVertex3D(-1.1f, 0.75f, -2.2f),
            RwVertex3D(1.1f, 0.75f, -2.2f),
            RwVertex3D(1.1f, 0.75f, 2.2f),
            RwVertex3D(-1.1f, 0.75f, 2.2f),
            // Cabin roof
            RwVertex3D(-0.9f, 1.35f, -0.6f),
            RwVertex3D(0.9f, 1.35f, -0.6f),
            RwVertex3D(0.9f, 1.35f, 0.8f),
            RwVertex3D(-0.9f, 1.35f, 0.8f),
            // Windshield base
            RwVertex3D(-1.05f, 0.75f, -0.8f),
            RwVertex3D(1.05f, 0.75f, -0.8f),
            // Rear window base
            RwVertex3D(-1.05f, 0.75f, 1.0f),
            RwVertex3D(1.05f, 0.75f, 1.0f)
        )

        val tris = listOf(
            // Front bumper
            RwTriangle(0, 1, 5), RwTriangle(0, 5, 4),
            // Rear bumper
            RwTriangle(3, 7, 6), RwTriangle(3, 6, 2),
            // Left side
            RwTriangle(0, 4, 7), RwTriangle(0, 7, 3),
            // Right side
            RwTriangle(1, 2, 6), RwTriangle(1, 6, 5),
            // Hood
            RwTriangle(4, 5, 13), RwTriangle(4, 13, 12),
            // Windshield
            RwTriangle(12, 13, 9), RwTriangle(12, 9, 8),
            // Roof
            RwTriangle(8, 9, 10), RwTriangle(8, 10, 11),
            // Rear window
            RwTriangle(11, 10, 15), RwTriangle(11, 15, 14),
            // Trunk
            RwTriangle(14, 15, 6), RwTriangle(14, 6, 7)
        )

        val materials = listOf(
            RwMaterial(0, Color(0xFFEF4444), 0.25f, 0.8f, 0.6f, "car1_001_COLOR_BASIC"),
            RwMaterial(1, Color(0xFF0F172A), 0.1f, 0.2f, 0.9f, null)
        )

        return RwClumpModel("car1.dff", "Vehicle", verts, tris, materials, 2.8f, 4)
    }

    private fun createPickupClump(): RwClumpModel {
        val verts = listOf(
            RwVertex3D(-1.15f, 0.3f, -2.4f),
            RwVertex3D(1.15f, 0.3f, -2.4f),
            RwVertex3D(1.15f, 0.3f, 2.4f),
            RwVertex3D(-1.15f, 0.3f, 2.4f),
            RwVertex3D(-1.15f, 0.9f, -2.4f),
            RwVertex3D(1.15f, 0.9f, -2.4f),
            RwVertex3D(1.15f, 0.9f, 2.4f),
            RwVertex3D(-1.15f, 0.9f, 2.4f),
            // Cab roof
            RwVertex3D(-1.0f, 1.6f, -1.0f),
            RwVertex3D(1.0f, 1.6f, -1.0f),
            RwVertex3D(1.0f, 1.6f, 0.4f),
            RwVertex3D(-1.0f, 1.6f, 0.4f)
        )
        val tris = listOf(
            RwTriangle(0, 1, 5), RwTriangle(0, 5, 4),
            RwTriangle(3, 7, 6), RwTriangle(3, 6, 2),
            RwTriangle(0, 4, 7), RwTriangle(0, 7, 3),
            RwTriangle(1, 2, 6), RwTriangle(1, 6, 5),
            RwTriangle(8, 9, 10), RwTriangle(8, 10, 11)
        )
        val mats = listOf(
            RwMaterial(0, Color(0xFF2563EB), 0.3f, 0.7f, 0.3f, "Pickup-Truck_Zombie_Atlas")
        )
        return RwClumpModel("Pickup-Truck.dff", "Vehicle", verts, tris, mats, 3.2f, 4)
    }

    private fun createCharacterClump(): RwClumpModel {
        val verts = listOf(
            // Feet
            RwVertex3D(-0.25f, 0f, 0f),
            RwVertex3D(0.25f, 0f, 0f),
            // Pelvis
            RwVertex3D(-0.25f, 0.9f, 0f),
            RwVertex3D(0.25f, 0.9f, 0f),
            // Shoulders
            RwVertex3D(-0.45f, 1.5f, 0f),
            RwVertex3D(0.45f, 1.5f, 0f),
            // Head
            RwVertex3D(0f, 1.85f, 0f),
            // Hands
            RwVertex3D(-0.6f, 1.0f, 0f),
            RwVertex3D(0.6f, 1.0f, 0f)
        )
        val tris = listOf(
            RwTriangle(0, 2, 3), RwTriangle(0, 3, 1),
            RwTriangle(2, 4, 5), RwTriangle(2, 5, 3),
            RwTriangle(4, 6, 5),
            RwTriangle(4, 7, 2), RwTriangle(5, 3, 8)
        )
        val mats = listOf(
            RwMaterial(0, Color(0xFF1E293B), 0.3f, 0.8f, 0.1f)
        )
        return RwClumpModel("Object_Character.dff", "Character", verts, tris, mats, 1.9f, 18)
    }

    private fun createSuzanneClump(): RwClumpModel {
        // Approximate Suzanne monkey head vertices
        val verts = mutableListOf<RwVertex3D>()
        val tris = mutableListOf<RwTriangle>()

        // Face & Brow
        verts.add(RwVertex3D(0f, 0f, 0.8f))       // 0: Nose tip
        verts.add(RwVertex3D(-0.4f, 0.4f, 0.5f))  // 1: Left eye
        verts.add(RwVertex3D(0.4f, 0.4f, 0.5f))   // 2: Right eye
        verts.add(RwVertex3D(0f, 0.7f, 0.4f))     // 3: Forehead center
        verts.add(RwVertex3D(-0.8f, 0.3f, 0.1f))  // 4: Left cheek
        verts.add(RwVertex3D(0.8f, 0.3f, 0.1f))   // 5: Right cheek
        verts.add(RwVertex3D(0f, -0.5f, 0.4f))    // 6: Chin
        verts.add(RwVertex3D(-1.2f, 0.6f, -0.3f)) // 7: Left ear tip
        verts.add(RwVertex3D(1.2f, 0.6f, -0.3f))  // 8: Right ear tip
        verts.add(RwVertex3D(0f, 0.8f, -0.6f))    // 9: Crown back
        verts.add(RwVertex3D(0f, -0.4f, -0.6f))   // 10: Neck back

        tris.add(RwTriangle(0, 1, 3))
        tris.add(RwTriangle(0, 3, 2))
        tris.add(RwTriangle(0, 4, 1))
        tris.add(RwTriangle(0, 2, 5))
        tris.add(RwTriangle(0, 6, 4))
        tris.add(RwTriangle(0, 5, 6))
        tris.add(RwTriangle(1, 7, 4))
        tris.add(RwTriangle(2, 5, 8))
        tris.add(RwTriangle(3, 7, 9))
        tris.add(RwTriangle(3, 9, 8))
        tris.add(RwTriangle(4, 10, 6))
        tris.add(RwTriangle(5, 6, 10))

        val mats = listOf(
            RwMaterial(0, Color(0xFFF59E0B), 0.4f, 0.8f, 0.5f)
        )
        return RwClumpModel("suzanne.dff", "Prop", verts, tris, mats, 1.4f, 1)
    }

    private fun createBuildingClump(): RwClumpModel {
        val verts = listOf(
            RwVertex3D(-4f, 0f, -4f),
            RwVertex3D(4f, 0f, -4f),
            RwVertex3D(4f, 0f, 4f),
            RwVertex3D(-4f, 0f, 4f),
            RwVertex3D(-4f, 16f, -4f),
            RwVertex3D(4f, 16f, -4f),
            RwVertex3D(4f, 16f, 4f),
            RwVertex3D(-4f, 16f, 4f)
        )
        val tris = listOf(
            RwTriangle(0, 1, 5), RwTriangle(0, 5, 4),
            RwTriangle(1, 2, 6), RwTriangle(1, 6, 5),
            RwTriangle(2, 3, 7), RwTriangle(2, 7, 6),
            RwTriangle(3, 0, 4), RwTriangle(3, 4, 7),
            RwTriangle(4, 5, 6), RwTriangle(4, 6, 7)
        )
        val mats = listOf(
            RwMaterial(0, Color(0xFF475569), 0.3f, 0.7f, 0.1f, "portland_brick_facade")
        )
        return RwClumpModel("portland_dock_bld01.dff", "Environment", verts, tris, mats, 18f, 1)
    }

    val samplePlacements = listOf(
        ItemPlacement(101, "portland_dock_bld01.dff", -28f, -28f, 0f, 1.2f, 1.2f, 1.2f, 350f),
        ItemPlacement(102, "portland_dock_bld01.dff", 28f, -28f, 0f, 1.4f, 1.4f, 1.8f, 400f),
        ItemPlacement(103, "portland_dock_bld01.dff", -28f, 28f, 0f, 1.0f, 1.0f, 1.1f, 300f),
        ItemPlacement(104, "portland_dock_bld01.dff", 28f, 28f, 0f, 1.6f, 1.6f, 2.2f, 450f),
        ItemPlacement(201, "car1.dff", 5f, 6f, 0f, 1f, 1f, 1f, 180f),
        ItemPlacement(202, "Pickup-Truck.dff", -8f, 12f, 0f, 1f, 1f, 1f, 180f),
        ItemPlacement(301, "suzanne.dff", 0f, -14f, 1.5f, 1.5f, 1.5f, 1.5f, 120f)
    )
}
