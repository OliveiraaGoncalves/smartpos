package com.raimundo.smartpos.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Módulo Kotlin puro (sem Android): `core:model`, `core:common`, `core:domain`. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("smartpos.quality")

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            // Sem toolchain explícito, o compileKotlin herda o JDK que está rodando o
            // Gradle (ex.: o JBR 21 do Android Studio) e diverge do compileJava (17).
            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(JavaLanguageVersion.of(17).asInt())
            }

            dependencies {
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", libs.findLibrary("truth").get())
                add("testImplementation", libs.findLibrary("turbine").get())
            }
        }
    }
}
