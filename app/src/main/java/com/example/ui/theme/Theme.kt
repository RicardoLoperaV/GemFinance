package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FinanzasDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextTertiaryDark,
    outline = OutlineDark,
    error = FoodRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme globally for maximum premium contrast
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the specific design identity
    content: @Composable () -> Unit,
) {
    val colorScheme = FinanzasDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
