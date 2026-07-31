package com.example.smarthome.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrownColorScheme = lightColorScheme(
    primary = WarmBrown,
    onPrimary = Color.White,
    primaryContainer = SandBrown,
    onPrimaryContainer = DarkBrown,
    secondary = SoftBrown,
    onSecondary = Color.White,
    secondaryContainer = CreamBrown,
    onSecondaryContainer = WarmBrown,
    background = Color.White,
    onBackground = DarkBrown,
    surface = Color.White,
    onSurface = DarkBrown,
    surfaceVariant = CreamBrown,
    onSurfaceVariant = WarmBrown
)

@Composable
fun SmartHomeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrownColorScheme,
        typography = Typography,
        content = content
    )
}
