package com.example.openliberty.engine3d

import androidx.compose.ui.graphics.Color
import kotlin.math.*

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vec3) = Vec3(x - v.x, y - v.y, z - v.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    operator fun div(s: Float) = Vec3(x / s, y / s, z / s)

    fun dot(v: Vec3): Float = x * v.x + y * v.y + z * v.z

    fun cross(v: Vec3): Vec3 = Vec3(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vec3 {
        val len = length()
        return if (len > 0.0001f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }
}

class Mat4(val m: FloatArray = FloatArray(16)) {

    companion object {
        fun identity(): Mat4 {
            val res = Mat4()
            res.m[0] = 1f; res.m[5] = 1f; res.m[10] = 1f; res.m[15] = 1f
            return res
        }

        fun translation(tx: Float, ty: Float, tz: Float): Mat4 {
            val res = identity()
            res.m[12] = tx
            res.m[13] = ty
            res.m[14] = tz
            return res
        }

        fun rotationY(radians: Float): Mat4 {
            val res = identity()
            val c = cos(radians)
            val s = sin(radians)
            res.m[0] = c;  res.m[2] = s
            res.m[8] = -s; res.m[10] = c
            return res
        }

        fun rotationX(radians: Float): Mat4 {
            val res = identity()
            val c = cos(radians)
            val s = sin(radians)
            res.m[5] = c;  res.m[6] = -s
            res.m[9] = s;  res.m[10] = c
            return res
        }

        fun rotationZ(radians: Float): Mat4 {
            val res = identity()
            val c = cos(radians)
            val s = sin(radians)
            res.m[0] = c;  res.m[1] = -s
            res.m[4] = s;  res.m[5] = c
            return res
        }

        fun lookAt(eye: Vec3, target: Vec3, up: Vec3): Mat4 {
            val f = (target - eye).normalize()
            val s = f.cross(up.normalize()).normalize()
            val u = s.cross(f)

            val res = Mat4()
            res.m[0] = s.x;  res.m[4] = s.y;  res.m[8] = s.z;   res.m[12] = -s.dot(eye)
            res.m[1] = u.x;  res.m[5] = u.y;  res.m[9] = u.z;   res.m[13] = -u.dot(eye)
            res.m[2] = -f.x; res.m[6] = -f.y; res.m[10] = -f.z; res.m[14] = f.dot(eye)
            res.m[3] = 0f;   res.m[7] = 0f;   res.m[11] = 0f;   res.m[15] = 1f
            return res
        }
    }

    fun multiply(b: Mat4): Mat4 {
        val res = Mat4()
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this.m[k * 4 + row] * b.m[col * 4 + k]
                }
                res.m[col * 4 + row] = sum
            }
        }
        return res
    }

    fun transform(v: Vec3): Vec3 {
        val x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]
        val y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]
        val z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]
        val w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15]
        return if (w != 0f && w != 1f) Vec3(x / w, y / w, z / w) else Vec3(x, y, z)
    }
}

data class Poly3D(
    val vertices: List<Vec3>,
    val baseColor: Color,
    val isDoubleSided: Boolean = false,
    val isEmissive: Boolean = false,
    val strokeColor: Color? = null,
    val strokeWidth: Float = 0f
) {
    fun calculateNormal(): Vec3 {
        if (vertices.size < 3) return Vec3(0f, 1f, 0f)
        val v0 = vertices[0]
        val v1 = vertices[1]
        val v2 = vertices[2]
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        return edge1.cross(edge2).normalize()
    }
}
