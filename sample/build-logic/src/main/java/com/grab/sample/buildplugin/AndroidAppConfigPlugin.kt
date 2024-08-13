package com.grab.sample.buildplugin

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

const val COMPILE_SDK = 34
const val JVM_TARGET = "11"

class AndroidAppConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("com.android.application")
            plugins.apply("org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                namespace = "com.grab.android.sample"
                compileSdk = COMPILE_SDK

                defaultConfig {
                    applicationId = "com.grab.android.sample"
                    minSdk = 21
                    targetSdk = 33
                    versionCode = 1
                    versionName = "0.0.1"
                    setProperty("archivesBaseName", "sample-bundle-file")
                }

                buildTypes {
                    debug {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                    release {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }

                buildFeatures {
                    viewBinding = true
                }

                signingConfigs {
                    create("release") {
                        storeFile = File("$rootDir/buildsystem/sample-release.keystore")
                        storePassword = "12345678"
                        keyAlias = "key0"
                        keyPassword = "12345678"
                    }
                }

                buildTypes {
                    release {
                        signingConfig = signingConfigs.getByName("release")
                    }
                }

                flavorDimensions += "service"
                productFlavors {
                    create("pro") {
                        dimension = "service"
                    }
                    create("gea") {
                        dimension = "service"
                    }
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