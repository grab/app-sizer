package com.grab.sizer.buildplugins

import com.grab.plugins.publish.MobilePublishExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** This plugin represents a build configuration
 * of java-libraries/kotlin modules.
 * These modules will require common build logic for unit tests and jacoco
 */
class AppSizerConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("org.jetbrains.kotlin.kapt")
        project.plugins.apply("com.grab.mobile-publish")

        project.the<MobilePublishExtension>().apply {
            groupId = "com.grab"
            version = if (System.getenv("CI") != null) "SNAPSHOT-05" else "SNAPSHOT"
        }

        project.tasks.withType(KotlinCompile::class.java).forEach {
            it.kotlinOptions.jvmTarget = "17"
        }

        project.tasks.withType(JavaCompile::class.java).configureEach {
            sourceCompatibility = JavaVersion.VERSION_17.toString()
            targetCompatibility = JavaVersion.VERSION_17.toString()
        }
    }
}