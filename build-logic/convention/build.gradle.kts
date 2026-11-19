plugins {
    `kotlin-dsl`
}

group = "com.raimundo.smartpos.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "smartpos.android.application"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "smartpos.android.library"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "smartpos.android.feature"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "smartpos.android.compose"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "smartpos.android.hilt"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "smartpos.android.room"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidRoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "smartpos.jvm.library"
            implementationClass = "com.raimundo.smartpos.buildlogic.JvmLibraryConventionPlugin"
        }
        register("androidTest") {
            id = "smartpos.android.test"
            implementationClass = "com.raimundo.smartpos.buildlogic.AndroidTestConventionPlugin"
        }
        register("quality") {
            id = "smartpos.quality"
            implementationClass = "com.raimundo.smartpos.buildlogic.QualityConventionPlugin"
        }
    }
}
