package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald80,
    onPrimary = Color(0xFF003820),
    primaryContainer = EmeraldGrey40,
    onPrimaryContainer = Emerald80,
    secondary = Mint80,
    onSecondary = Color(0xFF003822),
    background = SurfaceDark,
    surface = CardDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E2D26),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandEmerald,
    onPrimary = Color.White,
    primaryContainer = BrandEmeraldLight,
    onPrimaryContainer = BrandEmeraldDark,
    secondary = Mint40,
    onSecondary = Color.White,
    background = SurfaceLight,
    surface = CardLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F2),
    onSurfaceVariant = Color(0xFF475569),
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
