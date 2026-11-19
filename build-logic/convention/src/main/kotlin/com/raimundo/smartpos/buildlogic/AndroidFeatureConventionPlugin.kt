package com.raimundo.smartpos.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convenção para módulos `:feature:*`.
 *
 * Regra de dependência: feature -> core:ui, core:domain, core:model, core:common.
 * Nunca feature -> feature, nem feature -> core:data/core:network/core:local-storage.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("smartpos.android.library")
            pluginManager.apply("smartpos.android.compose")
            pluginManager.apply("smartpos.android.hilt")

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:common"))

                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-collections-immutable").get())

                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", libs.findLibrary("truth").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", project(":core:testing"))

                add("androidTestImplementation", project(":core:testing"))
                add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx-espresso-core").get())
            }
        }
    }
}
