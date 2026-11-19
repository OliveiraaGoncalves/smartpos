package com.raimundo.smartpos.buildlogic

import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Detekt + Spotless(ktlint) + Kover em todos os módulos, falhando o build em violação. */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("detekt").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("spotless").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("kover").get().get().pluginId)

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
                autoCorrect = false
            }

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    ktlint()
                    trimTrailingWhitespace()
                    endWithNewline()
                }
                kotlinGradle {
                    target("*.gradle.kts")
                    ktlint()
                }
            }
        }
    }
}
