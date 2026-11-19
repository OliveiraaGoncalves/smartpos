package com.raimundo.smartpos.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Papéis semânticos que o Material 3 não tem por padrão (sucesso/atenção), usados nos banners e chips. */
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

val LightExtendedColors =
    ExtendedColors(
        success = PetroleoSuccessLight,
        onSuccess = Color.White,
        successContainer = PetroleoSuccessContainerLight,
        onSuccessContainer = PetroleoSuccessLight,
        warning = PetroleoWarningLight,
        onWarning = Color.White,
        warningContainer = PetroleoWarningContainerLight,
        onWarningContainer = PetroleoWarningLight,
    )

val DarkExtendedColors =
    ExtendedColors(
        success = PetroleoSuccessDark,
        onSuccess = PetroleoSuccessContainerDark,
        successContainer = PetroleoSuccessContainerDark,
        onSuccessContainer = PetroleoSuccessDark,
        warning = PetroleoWarningDark,
        onWarning = PetroleoWarningContainerDark,
        warningContainer = PetroleoWarningContainerDark,
        onWarningContainer = PetroleoWarningDark,
    )

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
