package com.dangel.gelsoft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * D'Angel Gelsoft brand theme, taken from the client's real logo:
 * black background, teal/cyan accent, white/chrome text.
 * Deliberately NOT an olive/military-green palette — the brand is black + teal.
 */
private val Teal = Color(0xFF00D1C1)
private val TealDark = Color(0xFF00A99B)
private val Black = Color(0xFF000000)
private val SurfaceDark = Color(0xFF141414)
private val White = Color(0xFFFFFFFF)
private val LightGray = Color(0xFFBDBDBD)

private val DangelColorScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Black,
    primaryContainer = TealDark,
    onPrimaryContainer = White,
    secondary = Teal,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = SurfaceDark,
    onSurface = White,
    surfaceVariant = Color(0xFF1E1E1E),
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
