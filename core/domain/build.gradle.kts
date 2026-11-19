plugins {
    alias(libs.plugins.smartpos.jvm.library)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
}
