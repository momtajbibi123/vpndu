package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonPurple,
    tertiary = ConnectedGreen,
    background = DarkBase,
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnBackgroundLight,
    onSurface = OnBackgroundLight,
    error = WarningAmber
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark theme first
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our customized aesthetic consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
