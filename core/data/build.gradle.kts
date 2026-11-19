plugins {
    alias(libs.plugins.smartpos.android.library)
    alias(libs.plugins.smartpos.android.hilt)
    alias(libs.plugins.smartpos.android.test)
}

android {
    namespace = "com.raimundo.smartpos.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:local-storage"))
    implementation(project(":core:printer"))

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing"))
}
