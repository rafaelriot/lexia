package com.lexia.legalassistant.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LexiaPrimary,
    onPrimary = LexiaOnPrimary,
    primaryContainer = LexiaPrimaryContainer,
    onPrimaryContainer = LexiaOnPrimaryContainer,
    secondary = LexiaSecondary,
    onSecondary = LexiaOnSecondary,
    secondaryContainer = LexiaSecondaryContainer,
    onSecondaryContainer = LexiaOnSecondaryContainer,
    tertiary = LexiaTertiary,
    onTertiary = LexiaOnTertiary,
    tertiaryContainer = LexiaTertiaryContainer,
    onTertiaryContainer = LexiaOnTertiaryContainer,
    background = LexiaBackground,
    onBackground = LexiaOnBackground,
    surface = LexiaSurface,
    onSurface = LexiaOnSurface,
    surfaceVariant = LexiaSurfaceVariant,
    onSurfaceVariant = LexiaOnSurfaceVariant,
    outline = LexiaOutline,
    outlineVariant = LexiaOutlineVariant,
    error = LexiaError,
    onError = LexiaOnError,
    errorContainer = LexiaErrorContainer,
    onErrorContainer = LexiaOnErrorContainer
)

@Composable
fun LexiaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = LexiaSurface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LexiaTypography,
        shapes = LexiaShapes,
        content = content
    )
}
