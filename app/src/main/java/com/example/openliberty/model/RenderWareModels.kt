package com.example.openliberty.model

import androidx.compose.ui.graphics.Color

data class RwTexture(
    val name: String,
    val maskName: String = "",
    val width: Int,
    val height: Int,
    val depth: Int = 32,
    val format: String, // "DXT1", "DXT3", "RGBA8888"
    val mipmapCount: Int = 4,
    val previewColorPrimary: Color,
    val previewColorSecondary: Color,
    val description: String
)

data class RwMaterial(
    val id: Int,
    val color: Color,
    val ambient: Float = 0.3f,
    val diffuse: Float = 0.7f,
    val specular: Float = 0.2f,
    val textureName: String? = null
)

data class RwVertex3D(
    val x: Float,
    val y: Float,
    val z: Float,
    val u: Float = 0f,
    val v: Float = 0f
)

data class RwTriangle(
    val a: Int,
    val b: Int,
    val c: Int,
    val materialIndex: Int = 0
)

data class RwClumpModel(
    val name: String,
    val category: String, // "Vehicle", "Character", "Prop", "Environment"
    val vertices: List<RwVertex3D>,
    val triangles: List<RwTriangle>,
    val materials: List<RwMaterial>,
    val boundSphereRadius: Float = 2.5f,
    val frameCount: Int = 1
)

data class ItemPlacement(
    val id: Int,
    val modelName: String,
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val scaleZ: Float = 1f,
    val lodDistance: Float = 250f
)
