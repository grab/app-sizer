package com.grab.plugin.size

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.grab.plugin.size.utils.isAndroidApplication
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.kotlin.dsl.the


class AppSizePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.isAndroidApplication) {
            project.logger.log(LogLevel.ERROR, "The app size analysis only applicable to Android application")
            return
        }
        project.extensions.create("appSizeAnalysis", AppSizePluginExtension::class.java)
        project.plugins.withId("com.android.application") {
            configureAndroidComponents(project)
        }
    }

    private fun configureAndroidComponents(project: Project) {
        project.afterEvaluate {
            project.the<AppExtension>().applicationVariants.forEach { variant ->
                project.tasks.register("appSizeAnalysis${variant.name.capitalize()}", AppSizeAnalysisTask::class.java) {
                    dependsOn(variant.assembleProvider)
                    dependsOn("assemble${variant.name.capitalize()}")
                    this.variant = variant
                }
            }
        }
    }
}
