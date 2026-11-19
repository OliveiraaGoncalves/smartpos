plugins {
    // AGP 9+ tem suporte nativo a Kotlin: o plugin org.jetbrains.kotlin.android não é mais aplicado.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
