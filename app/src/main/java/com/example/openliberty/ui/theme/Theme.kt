package com.example.openliberty.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LibertyAmber = Color(0xFFF59E0B)
val LibertyAmberDark = Color(0xFFD97706)
val LibertyCyan = Color(0xFF06B6D4)
val LibertyRed = Color(0xFFEF4444)
val LibertyGreen = Color(0xFF10B981)

val DarkBackground = Color(0xFF0A0D14)
val DarkSurface = Color(0xFF131926)
val DarkSurfaceVariant = Color(0xFF1E283D)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOutline = Color(0xFF334155)

private val DarkColorScheme = darkColorScheme(
    primary = LibertyAmber,
    onPrimary = Color.Black,
    primaryContainer = LibertyAmberDark,
    onPrimaryContainer = Color.White,
    secondary = LibertyCyan,
    onSecondary = Color.Black,
    tertiary = LibertyRed,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkOutline
)

@Composable
fun OpenLibertyTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
