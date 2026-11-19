plugins {
    alias(libs.plugins.smartpos.android.library)
    alias(libs.plugins.smartpos.android.test)
}

android {
    namespace = "com.raimundo.smartpos.core.printer"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    testImplementation(project(":core:testing"))
}
