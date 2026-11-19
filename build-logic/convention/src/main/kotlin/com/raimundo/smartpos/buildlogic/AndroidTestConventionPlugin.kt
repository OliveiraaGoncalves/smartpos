package com.raimundo.smartpos.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Configura `androidTestImplementation` e os Gradle Managed Devices usados pelos testes instrumentados. */
class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<CommonExtension> {
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                testOptions.managedDevices {
                    localDevices.maybeCreate("pixel2api30").apply {
                        device = "Pixel 2"
                        apiLevel = 30
                        systemImageSource = "aosp-atd"
                    }
                }
            }

            dependencies {
                add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-runner").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-rules").get())
                add("androidTestImplementation", libs.findLibrary("androidx-espresso-core").get())
                add("androidTestImplementation", libs.findLibrary("truth").get())
            }
        }
    }
}
