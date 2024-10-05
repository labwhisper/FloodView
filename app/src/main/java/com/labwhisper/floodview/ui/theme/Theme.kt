package com.labwhisper.floodview.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightFloodColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryWhite,
    background = BackgroundLight,
    surface = SurfaceWhite,
)

val DarkFloodColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryWhite,
    background = Color(0xFF121212),
    surface = Color(0xFF1D1D1D),
)


@Composable
fun FloodViewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkFloodColorScheme else LightFloodColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}