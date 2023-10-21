package com.grab.plugin.size

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.grab.plugin.size.dependencies.projectsDependenciesSet
import com.grab.plugin.size.utils.*
import com.grab.tools.analyzer.report.ProjectInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.the
import java.io.File

internal const val PLUGIN_EXTENSION = "appSizeAnalysis"
private const val DEVICE_NAME_PARAM = "deviceName"
private const val PIPELINE_ID_PARAM = "pipeline"
private const val OPTION_PARAM = "option"
private const val LIBRARY_NAME_PARAM = "library"
internal const val DEVICE_SPEC_PARAM = "deviceSpec"

class AppSizePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.isAndroidApplication) {
            project.logger.log(LogLevel.ERROR, "The app size analysis only applicable to Android application")
            return
        }
        val extension = project.extensions.create(PLUGIN_EXTENSION, AppSizePluginExtension::class.java)
        project.plugins.withId(ANDROID_APPLICATION_PLUGIN) {
            configureAndroidComponents(project, extension)
        }
    }

    private fun configureAndroidComponents(project: Project, pluginExtension: AppSizePluginExtension) {
        project.afterEvaluate {
            project.the<AppExtension>().applicationVariants.forEach { variant ->
                val apkDirectory = File("${variant.outputs.first().outputFile.parent}/apks")
                val generateApkTask =
                    project.tasks.register("generateApkFor${variant.name.capitalize()}", GenerateApkTask::class.java) {
                        dependsOn("bundle${variant.name.capitalize()}")
                        deviceSpecFilePath.set(project.findProperty(DEVICE_SPEC_PARAM) as String?)
                        bundleToolPath.set(pluginExtension.bundleToolPath.get())
                        outputDirectory.set(apkDirectory)
                        bundleFile.set(file(pluginExtension.bundleFilePath))
                        signingConfig.set(variant.signingConfig.toInternalSigningConfig())
                    }

                val appSizeAnalysisTask = project.tasks.register(
                    "appSizeAnalysis${variant.name.capitalize()}", AppSizeAnalysisTask::class.java
                ) {
                    dependsOn(generateApkTask)
                    extension = pluginExtension
                    apksDirectory.set(apkDirectory)
                    libName.set(project.findProperty(LIBRARY_NAME_PARAM) as String?)
                    option.set(project.findProperty(OPTION_PARAM) as String?)
                    projectInfo.set(extractProjectInfo(project, variant, extension))
                    this.variant = variant
                }

                projectsDependenciesSet(project, variant)
                    .forEach {
                        handleSubProject(it, appSizeAnalysisTask, variant)
                    }
            }
        }
    }

    private fun extractProjectInfo(
        project: Project,
        variant: BaseVariant,
        extension: AppSizePluginExtension
    ): ProjectInfo {
        return ProjectInfo(
            projectName = project.rootProject.displayName,
            versionName = variant.mergedFlavor.versionName ?: "NA",
            deviceName = (project.findProperty(DEVICE_NAME_PARAM) as String?) ?: DEFAULT_DEVICE_NAME,
            pipelineId = project.findProperty(PIPELINE_ID_PARAM) as String?,
            buildType = variant.name,
            tag = extension.tag.get()
        )
    }

    private fun handleSubProject(project: Project, task: TaskProvider<AppSizeAnalysisTask>, variant: BaseVariant) {
        when {
            project.isAndroidLibrary -> {
                val libraryVariants = project.the<LibraryExtension>().libraryVariants
                val libVariant = libraryVariants.find {
                    it.flavorName == variant.flavorName && it.buildType.name == variant.buildType.name
                } ?: libraryVariants.find {
                    it.buildType.name == variant.buildType.name
                    /** Note that if there is no match with build type, the current solution is to select the first variant
                     * This should be re-visit
                     */
                } ?: libraryVariants.first()
                task.dependsOn(libVariant.assembleProvider)
            }
            project.isKotlinJvm -> {
                task.dependsOn(project.tasks.named("assemble"))
            }
            project.isJava -> {
                task.dependsOn(project.tasks.named("assemble"))
            }
        }

    }
}
