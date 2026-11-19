plugins {
    alias(libs.plugins.smartpos.jvm.library)
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing"))
}
