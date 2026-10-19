package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = PureWhite,
    primaryContainer = CrimsonRedContainer,
    onPrimaryContainer = OnCrimsonRedContainer,
    secondary = CrimsonRedLight,
    onSecondary = CharcoalBackground,
    tertiary = NeutralGrayText,
    onTertiary = PureWhite,
    background = CharcoalBackground,
    onBackground = PureWhite,
    surface = CharcoalSurface,
    onSurface = PureWhite,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = NeutralGrayText,
    outline = CharcoalBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonRedDark,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF991B1B),
    onSecondary = PureWhite,
    tertiary = LightTextSecondary,
    onTertiary = PureWhite,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun ZaswixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZaswixTheme(darkTheme = darkTheme, content = content)
}
