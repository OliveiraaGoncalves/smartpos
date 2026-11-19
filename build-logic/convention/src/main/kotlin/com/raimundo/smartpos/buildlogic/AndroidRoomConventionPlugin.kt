package com.raimundo.smartpos.buildlogic

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("ksp").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("room").get().get().pluginId)

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                // api: módulos que consomem SmartPosDatabase diretamente (core:data) precisam
                // de RoomDatabase no classpath de compilação.
                add("api", libs.findLibrary("androidx-room-runtime").get())
                add("api", libs.findLibrary("androidx-room-ktx").get())
                add("ksp", libs.findLibrary("androidx-room-compiler").get())
                add("testImplementation", libs.findLibrary("androidx-room-testing").get())
                add("androidTestImplementation", libs.findLibrary("androidx-room-testing").get())
            }
        }
    }
}
