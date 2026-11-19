plugins {
    alias(libs.plugins.smartpos.android.library)
    alias(libs.plugins.smartpos.android.room)
    alias(libs.plugins.smartpos.android.test)
}

android {
    namespace = "com.raimundo.smartpos.core.localstorage"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))
}
