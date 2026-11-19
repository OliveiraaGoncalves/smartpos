package com.raimundo.smartpos.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.raimundo.smartpos.core.model.ThemeMode

private val LightColors =
    lightColorScheme(
        primary = PetroleoPrimaryLight,
        onPrimary = PetroleoOnPrimaryLight,
        primaryContainer = PetroleoPrimaryContainerLight,
        onPrimaryContainer = PetroleoOnPrimaryContainerLight,
        background = PetroleoBackgroundLight,
        onBackground = PetroleoOnSurfaceLight,
        surface = PetroleoSurfaceLight,
        onSurface = PetroleoOnSurfaceLight,
        surfaceVariant = PetroleoSurfaceVariantLight,
        onSurfaceVariant = PetroleoOnSurfaceVariantLight,
        outline = PetroleoOutlineLight,
        error = PetroleoErrorLight,
        errorContainer = PetroleoErrorContainerLight,
    )

private val DarkColors =
    darkColorScheme(
        primary = PetroleoPrimaryDark,
        onPrimary = PetroleoOnPrimaryDark,
        primaryContainer = PetroleoPrimaryContainerDark,
        onPrimaryContainer = PetroleoOnPrimaryContainerDark,
        background = PetroleoBackgroundDark,
        onBackground = PetroleoOnSurfaceDark,
        surface = PetroleoSurfaceDark,
        onSurface = PetroleoOnSurfaceDark,
        surfaceVariant = PetroleoSurfaceVariantDark,
        onSurfaceVariant = PetroleoOnSurfaceVariantDark,
        outline = PetroleoOutlineDark,
        error = PetroleoErrorDark,
        errorContainer = PetroleoErrorContainerDark,
    )

@Composable
fun SmartPosTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SmartPosTypography,
            shapes = SmartPosShapes,
            content = content,
        )
    }
}

/** Acesso aos papéis de cor extras (sucesso/atenção) dentro de um [SmartPosTheme]. */
object SmartPosColors {
    val extended: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
