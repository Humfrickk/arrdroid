package com.example.arrdroid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

// ── Lidarr-inspirierte Farben ────────────────────────────────────────
private val Green400 = Color(0xFF66BB6A)
private val Green300 = Color(0xFF81C784)
private val Green700 = Color(0xFF388E3C)
private val SurfaceDark = Color(0xFF121212)
private val SurfaceContainer = Color(0xFF1E1E1E)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val OnSurface = Color(0xFFE0E0E0)
private val OnSurfaceVariant = Color(0xFF9E9E9E)
private val ErrorRed = Color(0xFFEF5350)

private val ArrdroidDarkColors = darkColorScheme(
    primary = Green400,
    onPrimary = Color.Black,
    primaryContainer = Green700,
    onPrimaryContainer = Green300,
    secondary = Color(0xFF80CBC4),
    onSecondary = Color.Black,
    background = SurfaceDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLow = Color(0xFF171717),
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    error = ErrorRed,
    onError = Color.Black,
    outline = Color(0xFF555555),
    outlineVariant = Color(0xFF333333),
)

private val ArrdroidTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun ArrdroidTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SurfaceDark.toArgb()
            window.navigationBarColor = SurfaceDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ArrdroidDarkColors,
        typography = ArrdroidTypography,
        content = content
    )
}

