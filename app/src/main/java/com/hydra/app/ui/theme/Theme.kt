package com.hydra.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HydraBlue80,
    onPrimary = HydraBlue20,
    primaryContainer = HydraBlue30,
    onPrimaryContainer = HydraBlue90,
    secondary = HydraCyan80,
    onSecondary = HydraCyan20,
    secondaryContainer = HydraCyan30,
    onSecondaryContainer = HydraCyan90,
    tertiary = HydraTeal80,
    onTertiary = HydraTeal20,
    tertiaryContainer = HydraTeal30,
    onTertiaryContainer = HydraTeal90
)

private val LightColorScheme = lightColorScheme(
    primary = HydraBlue40,
    onPrimary = HydraBlue90,
    primaryContainer = HydraBlue90,
    onPrimaryContainer = HydraBlue10,
    secondary = HydraCyan40,
    onSecondary = HydraCyan90,
    secondaryContainer = HydraCyan90,
    onSecondaryContainer = HydraCyan10,
    tertiary = HydraTeal40,
    onTertiary = HydraTeal90,
    tertiaryContainer = HydraTeal90,
    onTertiaryContainer = HydraTeal10
)

@Composable
fun HydraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
