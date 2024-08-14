package com.grab.sample.buildplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinLibraryConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("java-library")
            plugins.apply("org.jetbrains.kotlin.jvm")

            extensions.getByType<JavaPluginExtension>().apply {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }

            extensions.getByType<KotlinJvmProjectExtension>().apply {
                jvmToolchain(JAVA_VERSION.toString().toInt())
            }

            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    jvmTarget = JVM_TARGET
                }
            }
        }
    }
}