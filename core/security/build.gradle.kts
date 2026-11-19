plugins {
    alias(libs.plugins.smartpos.android.library)
    alias(libs.plugins.smartpos.android.test)
}

android {
    namespace = "com.raimundo.smartpos.core.security"
}

dependencies {
    implementation(project(":core:model"))

    testImplementation(project(":core:testing"))
}
