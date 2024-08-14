package com.grab.sample.buildplugin

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal const val COMPILE_SDK = 34
internal const val JVM_TARGET = "17"
internal val JAVA_VERSION = JavaVersion.VERSION_17
internal const val MIN_SDK = 21
internal const val TARGET_SDK = 33

class AndroidAppConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("com.android.application")
            plugins.apply("org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                compileSdk = COMPILE_SDK

                defaultConfig {
                    applicationId = "com.grab.android.sample"
                    minSdk = MIN_SDK
                    targetSdk = TARGET_SDK
                }
                JAVA_VERSION

                compileOptions {
                    sourceCompatibility = JAVA_VERSION
                    targetCompatibility = JAVA_VERSION
                }
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = JVM_TARGET
                }
            }
        }
    }
}