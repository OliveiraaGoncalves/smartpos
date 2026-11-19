plugins {
    alias(libs.plugins.smartpos.android.library)
}

android {
    namespace = "com.raimundo.smartpos.core.networkmock"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:security"))
    implementation(project(":core:network"))

    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
}
