package com.dangel.gelsoft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * D'Angel Gelsoft brand theme v3.1: soft blacks (no pure black) and a
 * muted teal (less neon). Brand stays black + teal, just calmer.
 */
private val Teal = Color(0xFF3FA79B)
private val TealDark = Color(0xFF2E7D74)
private val BgSoft = Color(0xFF121212)
private val SurfaceSoft = Color(0xFF1C1C1C)
private val SurfaceVariantSoft = Color(0xFF262626)
private val White = Color(0xFFFFFFFF)
private val LightGray = Color(0xFFBDBDBD)

private val DangelColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Color(0xFF0B0B0B),
    primaryContainer = TealDark,
    onPrimaryContainer = White,
    secondary = Teal,
    onSecondary = Color(0xFF0B0B0B),
    background = BgSoft,
    onBackground = White,
    surface = SurfaceSoft,
    onSurface = White,
    surfaceVariant = SurfaceVariantSoft,
    onSurfaceVariant = LightGray,
    outline = Color(0xFF3A3A3A),
)

@Composable
fun DangelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DangelColorScheme,
        typography = Typography(),
        content = content,
    )
}
