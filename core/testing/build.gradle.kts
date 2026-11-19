plugins {
    alias(libs.plugins.smartpos.android.library)
}

android {
    namespace = "com.raimundo.smartpos.core.testing"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:domain"))

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.truth)
    api(libs.turbine)
    api(libs.androidx.test.ext.junit)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.rules)
    api(libs.hilt.android.testing)
}
