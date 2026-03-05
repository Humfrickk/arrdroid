package com.example.arrdroid.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.core.view.WindowCompat

// ── Google Fonts Provider ────────────────────────────────────────────
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.example.arrdroid.R.array.com_google_android_gms_fonts_certs
)

val RobotoMonoFont = GoogleFont("Roboto Mono")

val RobotoMono = FontFamily(
    Font(googleFont = RobotoMonoFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = RobotoMonoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = RobotoMonoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = RobotoMonoFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = RobotoMonoFont, fontProvider = provider, weight = FontWeight.Bold),
)

// ── Orange → Lila Gradient Colors ────────────────────────────────────
val Orange = Color(0xFFFF8C00)           // Deep Orange
val OrangeLight = Color(0xFFFFAB40)      // Light Orange
val Lila = Color(0xFFAB47BC)             // Purple/Lila
val LilaLight = Color(0xFFCE93D8)        // Light Lila
val LilaDark = Color(0xFF7B1FA2)         // Dark Purple

val DarkBackground = Color(0xFF1A1A1A)   // Dark gray background
val DarkSurface = Color(0xFF1A1A1A)      // Same as background
val DarkSurfaceContainer = Color(0xFF242424)
val DarkSurfaceContainerHigh = Color(0xFF2E2E2E)
val OnSurface = Color(0xFFE8E8E8)
val OnSurfaceVariant = Color(0xFFA0A0A0)
val ErrorRed = Color(0xFFEF5350)

private val ArrdroidDarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF6D3A00),
    onPrimaryContainer = OrangeLight,
    secondary = LilaLight,
    onSecondary = Color.Black,
    secondaryContainer = LilaDark,
    onSecondaryContainer = LilaLight,
    tertiary = Lila,
    onTertiary = Color.White,
    tertiaryContainer = LilaDark,
    onTertiaryContainer = LilaLight,
    background = DarkBackground,
    onBackground = OnSurface,
    surface = DarkSurface,
    onSurface = OnSurface,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
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
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
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
