plugins {
    alias(libs.plugins.smartpos.android.library)
    alias(libs.plugins.smartpos.android.test)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.raimundo.smartpos.core.security"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
}
