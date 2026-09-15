package com.pharmachain.ai.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PharmaPrimary,
    onPrimary = PharmaOnPrimary,
    primaryContainer = PharmaPrimaryContainer,
    onPrimaryContainer = PharmaOnPrimaryContainer,
    inversePrimary = PharmaInversePrimary,
    secondary = PharmaSecondary,
    onSecondary = PharmaOnSecondary,
    secondaryContainer = PharmaSecondaryContainer,
    onSecondaryContainer = PharmaOnSecondaryContainer,
    tertiary = PharmaTertiary,
    onTertiary = PharmaOnTertiary,
    tertiaryContainer = PharmaTertiaryContainer,
    onTertiaryContainer = PharmaOnTertiaryContainer,
    background = PharmaBackgroundLight,
    onBackground = PharmaOnBackgroundLight,
    surface = PharmaSurfaceLight,
    onSurface = PharmaOnSurfaceLight,
    surfaceVariant = PharmaSurfaceVariantLight,
    onSurfaceVariant = PharmaOnSurfaceVariantLight,
    outline = PharmaOutlineLight,
    outlineVariant = PharmaOutlineVariantLight,
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorBg,
    onErrorContainer = StatusErrorText
)

private val DarkColorScheme = darkColorScheme(
    primary = PharmaInversePrimary,
    onPrimary = PharmaOnPrimaryContainer,
    primaryContainer = PharmaPrimary,
    onPrimaryContainer = PharmaPrimaryContainer,
    secondary = PharmaSecondaryContainer,
    onSecondary = PharmaOnSecondaryContainer,
    secondaryContainer = PharmaSecondary,
    onSecondaryContainer = PharmaSecondaryContainer,
    tertiary = PharmaTertiaryContainer,
    onTertiary = PharmaOnTertiaryContainer,
    tertiaryContainer = PharmaTertiary,
    onTertiaryContainer = PharmaTertiaryContainer,
    background = PharmaBackgroundDark,
    onBackground = PharmaOnBackgroundDark,
    surface = PharmaSurfaceDark,
    onSurface = PharmaOnSurfaceDark,
    surfaceVariant = PharmaSurfaceVariantDark,
    onSurfaceVariant = PharmaOnSurfaceVariantDark,
    outline = PharmaOutlineDark,
    outlineVariant = PharmaOutlineDark,
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorBg,
    onErrorContainer = StatusErrorText
)

@Composable
fun PharmaChainTheme(
    darkTheme: Boolean = false, // Healthcare B2B default: Light theme on launch, ignoring system dark mode for bright pharmacy lighting
    dynamicColor: Boolean = false, // Keep false to maintain strict clinical visual consistency
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // In light mode (!darkTheme), status and navigation bars use dark icons over light backgrounds
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
