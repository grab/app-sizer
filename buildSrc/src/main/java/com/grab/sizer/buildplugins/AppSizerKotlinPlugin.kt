package com.grab.sizer.buildplugins

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AppSizerKotlinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType(KotlinCompile::class.java).forEach {
            it.kotlinOptions.jvmTarget = "11"
        }

        project.tasks.withType(JavaCompile::class.java).configureEach {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}