/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.plugin.sizer

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.grab.plugin.sizer.configuration.DefaultVariantFilter
import com.grab.plugin.sizer.dependencies.*
import com.grab.plugin.sizer.tasks.AppSizeAnalysisTask
import com.grab.plugin.sizer.tasks.GenerateApkTask
import com.grab.plugin.sizer.tasks.GenerateArchivesListTask
import com.grab.plugin.sizer.utils.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider

/*
 * This internal `TaskManager` class is used for configuring tasks for the Plugin
 *
 * The `TaskManager` class is responsible for setting up tasks based on plugin extensions.
 * It evaluates and applies tasks to projects based on the configuration found in a provided `AppSizePluginExtension`.
 */
internal class TaskManager(
    private val project: Project,
    private val pluginExtension: AppSizePluginExtension,
    private val logger: PluginLogger
) {
    fun configTasks() {
        project.pluginManager.withPlugin(ANDROID_APPLICATION_PLUGIN) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                // onVariants callbacks run after the build script is evaluated,
                // so the appSizer extension is fully configured at this point
                if (pluginExtension.enabled) {
                    configAppSizeTask(project, variant)
                }
            }
        }
    }

    private fun configAppSizeTask(project: Project, variant: ApplicationVariant) {
        val variantFilter = DefaultVariantFilter(variant)
        pluginExtension.input.variantFilter?.execute(variantFilter)
        if (variantFilter.ignored) return

        val android = project.extensions.getByType(ApplicationExtension::class.java)
        val variantInput = variant.toVariantInput()
        val flavorMatchingFallbacks = variant.flavorMatchingFallbacks(android)
        val buildTypeMatchingFallbacks = variant.buildTypeMatchingFallbacks(android)

        val generateApkTask = GenerateApkTask.registerTask(
            project,
            pluginExtension,
            variant,
            android
        )

        val generateArchivesListTask = GenerateArchivesListTask.registerTask(
            project,
            variantInput = variantInput,
            flavorMatchingFallbacks = flavorMatchingFallbacks,
            buildTypeMatchingFallbacks = buildTypeMatchingFallbacks,
            enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant
        )

        val appSizeAnalysisTask = AppSizeAnalysisTask.registerTask(
            project,
            variant,
            variantInput,
            pluginExtension,
            generateApkTask,
            generateArchivesListTask,
        )

        /*
         * The analysis task consumes the AAR/JAR binaries of all dependency projects. Their task
         * dependencies are wired after every project has been evaluated so the whole project
         * graph (plugins & configurations) is known.
         */
        project.rootProject.gradle.projectsEvaluated {
            registerAppSizeTaskDep(
                project,
                variantInput,
                flavorMatchingFallbacks,
                buildTypeMatchingFallbacks,
                appSizeAnalysisTask
            )
        }
    }

    private fun registerAppSizeTaskDep(
        project: Project,
        variantInput: VariantInput,
        flavorMatchingFallbacks: List<String>,
        buildTypeMatchingFallbacks: List<String>,
        depTask: TaskProvider<out Task>
    ) {
        val dependenciesComponent = DaggerDependenciesComponent.factory().create(
            project = project,
            variantInput = variantInput,
            flavorMatchingFallbacks = flavorMatchingFallbacks,
            buildTypeMatchingFallbacks = buildTypeMatchingFallbacks,
            enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant
        )
        val markAsChecked = mutableSetOf<String>()
        dfs(project, markAsChecked, dependenciesComponent, depTask)
    }

    private fun dfs(
        project: Project,
        markAsChecked: MutableSet<String>,
        dependenciesComponent: DependenciesComponent,
        depTask: TaskProvider<out Task>
    ) {
        if (markAsChecked.contains(project.path)) return
        markAsChecked.add(project.path)
        handleSubProject(project, depTask, dependenciesComponent.variantExtractor())
        dependenciesComponent.configurationExtractor()
            .runtimeConfigurations(project)
            .flatMap { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java)
            }.forEach {
                dfs(project.project(it.path), markAsChecked, dependenciesComponent, depTask)
            }
    }

    private fun handleSubProject(
        project: Project,
        task: TaskProvider<out Task>,
        variantExtractor: VariantExtractor
    ) {
        when {
            project.isAndroidLibrary -> {
                try {
                    val variant = variantExtractor.findMatchVariant(project)
                    if (variant is AndroidAppSizeVariant) {
                        val assembleTask = project.tasks.named(variant.variant.assembleTaskName)
                        task.configure { it.dependsOn(assembleTask) }
                    }
                } catch (e: UnsupportedOperationException) {
                    logger.warn("Unsupported project type for Android library project ${project.name}: ${e.message}")
                    logger.debug("Full stack trace for variant extraction failure:", e)
                } catch (e: IllegalStateException) {
                    logger.warn("Could not find matching variant for Android library project ${project.name}: ${e.message}")
                    logger.debug("Full stack trace for variant extraction failure:", e)
                }
            }

            project.isKotlinJvm || project.isJava -> {
                val jarTask = project.tasks.named(JavaPlugin.JAR_TASK_NAME)
                task.configure { it.dependsOn(jarTask) }
            }

            project.isKotlinMultiplatform -> {
                val jarTask = project.tasks.named(KMP_JAR_TASK)
                task.configure { it.dependsOn(jarTask) }
            }

            else -> {
                // Skip unsupported project types to avoid variant extraction errors
                logger.warn("Skipping variant extraction for unsupported project type: ${project.name}")
            }
        }

    }
}

internal fun ApplicationVariant.toVariantInput() = VariantInput(
    name = name,
    flavorName = flavorName.orEmpty(),
    buildTypeName = buildType.orEmpty(),
)

internal fun ApplicationVariant.flavorMatchingFallbacks(android: ApplicationExtension): List<String> =
    productFlavors.firstNotNullOfOrNull { (_, flavorName) ->
        android.productFlavors.findByName(flavorName)?.matchingFallbacks?.takeIf { it.isNotEmpty() }
    } ?: emptyList()

internal fun ApplicationVariant.buildTypeMatchingFallbacks(android: ApplicationExtension): List<String> =
    buildType?.let { android.buildTypes.findByName(it)?.matchingFallbacks } ?: emptyList()
