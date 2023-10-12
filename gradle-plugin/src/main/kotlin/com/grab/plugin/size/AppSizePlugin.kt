package com.grab.plugin.size

import com.android.build.gradle.AppExtension
import com.grab.plugin.size.utils.isAndroidApplication
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.kotlin.dsl.the
import java.io.File


class AppSizePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.isAndroidApplication) {
            project.logger.log(LogLevel.ERROR, "The app size analysis only applicable to Android application")
            return
        }
        val extension = project.extensions.create("appSizeAnalysis", AppSizePluginExtension::class.java)
        project.plugins.withId("com.android.application") {
            configureAndroidComponents(project, extension)
        }
    }

    private fun configureAndroidComponents(project: Project, extension: AppSizePluginExtension) {
        project.afterEvaluate {

            project.the<AppExtension>().applicationVariants.forEach { variant ->
                val generateApkTask =
                    project.tasks.register("generateApkFor${variant.name.capitalize()}", GenerateApkTask::class.java) {
//                        dependsOn("bundle${variant.name.capitalize()}")
                        androidDeviceConfig.set(extension.deviceConfig.get())
                        bundleToolPath.set(extension.bundleToolPath.get())
                        outputFile.set(File("${variant.outputs.first().outputFile.parent}/app.apks"))
                        bundleFile.set(file(extension.bundleFilePath))
                        signingConfig.set(variant.signingConfig.toInternalSigningConfig())
                    }

                project.tasks.register("appSizeAnalysis${variant.name.capitalize()}", AppSizeAnalysisTask::class.java) {
                    dependsOn(generateApkTask)
                    dependsOn(variant.assembleProvider)
//                    dependsOn("assemble${variant.name.capitalize()}")
                    this.variant = variant
                }
            }
        }
    }
}
