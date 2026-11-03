package com.jastin.boveda.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* =========================================================================
* MATERIAL THEME WRAPPER (GLOBAL DESIGN SYSTEM)
 * Maps our color tokens to Material3 semantic roles,
 * allowing standard components to automatically adopt our
 * visual brand identity.
 * ========================================================================= */
private val LightColorScheme = lightColorScheme(
    primary = Emerald500,
    onPrimary = Slate950,
    secondary = Teal400,
    background = Slate50,
    surface = Color.White,
    onSurface = Slate900
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Color.White,
    secondary = Teal400,
    background = PlomoBackground,
    surface = PlomoSurface,
    onSurface = PlomoText
)

@Composable
fun BovedaTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}