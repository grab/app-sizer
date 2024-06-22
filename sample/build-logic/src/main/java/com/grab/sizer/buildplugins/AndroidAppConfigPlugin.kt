package com.grab.sizer.buildplugins

import com.grab.plugins.publish.MobilePublishExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


class AndroidAppConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("org.jetbrains.kotlin.kapt")
        project.plugins.apply("com.grab.mobile-publish")

        project.the<MobilePublishExtension>().apply {
            groupId = "com.grab"
            version = if (System.getenv("CI") != null) "SNAPSHOT-07" else "SNAPSHOT"
        }

        project.tasks.withType(KotlinCompile::class.java).forEach {
            it.kotlinOptions.jvmTarget = "11"
        }

        project.tasks.withType(JavaCompile::class.java).configureEach {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}