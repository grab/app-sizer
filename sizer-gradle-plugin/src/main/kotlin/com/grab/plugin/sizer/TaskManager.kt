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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.grab.plugin.sizer.configuration.DefaultVariantFilter
import com.grab.plugin.sizer.dependencies.*
import com.grab.plugin.sizer.tasks.AppSizeAnalysisTask
import com.grab.plugin.sizer.tasks.GenerateApkTask
import com.grab.plugin.sizer.tasks.GenerateArchivesListTask
import com.grab.plugin.sizer.tasks.capitalize
import com.grab.plugin.sizer.utils.isAndroidApplication
import com.grab.plugin.sizer.utils.isAndroidLibrary
import com.grab.plugin.sizer.utils.isJava
import com.grab.plugin.sizer.utils.isKotlinJvm
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.Directory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.the

/*
 * This internal `TaskManager` class is used for configuring tasks for the Plugin
 *
 * The `TaskManager` class is responsible for setting up tasks based on plugin extensions.
 * It evaluates and applies tasks to projects based on the configuration found in a provided `AppSizePluginExtension`.
 */
internal class TaskManager(
    private val project: Project,
    private val pluginExtension: AppSizePluginExtension
) {
    fun configTasks() {
        project.rootProject.gradle.projectsEvaluated {
            if (pluginExtension.enabled && project.isAndroidApplication) {
                configAppSizeTask(project)
            }
        }
    }

    private fun configAppSizeTask(project: Project) {
        with(project.the<AppExtension>()) {
            applicationVariants.forEach { variant ->
                val variantFilter = DefaultVariantFilter(variant)
                pluginExtension.input.variantFilter?.execute(variantFilter)
                if (!variantFilter.ignored) {

                    val generateArchivesListTask = GenerateArchivesListTask.registerTask(
                        project = project,
                        variant = variant,
                        flavorMatchingFallbacks = getProductFlavor(variant)?.matchingFallbacks ?: emptyList(),
                        buildTypeMatchingFallbacks = getOriginalBuildType(variant).matchingFallbacks,
                        enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant
                    )

                    runCatching {
                        registerCollectDependenciesTask(project, variant, this)
                    }.onFailure {
                        project.logger.error("Can't create tasks for ${project.name} with variant ${variant.name}")
                    }.onSuccess { collectAppDependenciesTask ->
                        createAabAnalysisTask(project, variant, generateArchivesListTask).configure {
                            dependsOn(collectAppDependenciesTask)
                        }
                        createApkAnalysisTask(project, variant, generateArchivesListTask).configure {
                            dependsOn(collectAppDependenciesTask)
                        }
                    }
                }
            }
        }
    }

    private fun createApkAnalysisTask(
        project: Project,
        variant: ApplicationVariant,
        generateArchivesListTask: TaskProvider<GenerateArchivesListTask>
    ) = AppSizeAnalysisTask.registerTask(
        name = "apk",
        project = project,
        variant = variant,
        pluginExtension = pluginExtension,
        apkDirectories = variant.packageApplicationProvider.map {
            project.objects.listProperty<Directory>().value(listOf(it.outputDirectory.get()))
        },
        generateArchivesListTask = generateArchivesListTask,
    )

    private fun createAabAnalysisTask(
        project: Project,
        variant: ApplicationVariant,
        generateArchivesListTask: TaskProvider<GenerateArchivesListTask>
    ): TaskProvider<AppSizeAnalysisTask> {
        val generateApkFromAabTask = GenerateApkTask.registerTask(
            project,
            pluginExtension,
            variant
        )

        val appSizeAnalysisTask = AppSizeAnalysisTask.registerTask(
            name = "app",
            project = project,
            variant = variant,
            pluginExtension = pluginExtension,
            apkDirectories = generateApkFromAabTask.map { it.outputDirectories },
            generateArchivesListTask = generateArchivesListTask,
        )

        return appSizeAnalysisTask
    }

    private fun registerCollectDependenciesTask(
        project: Project,
        variant: BaseVariant,
        appExtension: AppExtension,
    ): TaskProvider<Task> {
        val dependenciesComponent = DaggerDependenciesComponent.factory().create(
            project = project,
            variantInput = variant.toVariantInput(),
            flavorMatchingFallbacks = appExtension.getProductFlavor(variant)?.matchingFallbacks ?: emptyList(),
            buildTypeMatchingFallbacks = appExtension.getOriginalBuildType(variant).matchingFallbacks,
            enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant
        )
        val collectDependenciesTask = project.tasks.register("collectAppDependencies${variant.name.capitalize()}")

        val markAsChecked = mutableSetOf<String>()
        dfs(project, markAsChecked, dependenciesComponent, collectDependenciesTask)

        return collectDependenciesTask
    }

    private fun dfs(
        project: Project,
        markAsChecked: MutableSet<String>,
        dependenciesComponent: DependenciesComponent,
        depTask: TaskProvider<Task>
    ) {
        if (markAsChecked.contains(project.path)) return
        markAsChecked.add(project.path)
        handleSubProject(project, depTask, dependenciesComponent.variantExtractor())
        dependenciesComponent.configurationExtractor()
            .runtimeConfigurations(project)
            .flatMap { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java)
            }.forEach {
                dfs(it.dependencyProject, markAsChecked, dependenciesComponent, depTask)
            }
    }

    private fun handleSubProject(
        project: Project,
        task: TaskProvider<Task>,
        variantExtractor: VariantExtractor
    ) {
        when {
            project.isAndroidLibrary -> {
                val variant = variantExtractor.findMatchVariant(project)
                if (variant is AndroidAppSizeVariant) {
                    task.dependsOn(variant.baseVariant.assembleProvider)
                }
            }

            project.isKotlinJvm -> {
                task.dependsOn(project.tasks.named("jar"))
            }

            project.isJava -> {
                task.dependsOn(project.tasks.named("jar"))
            }
        }

    }
}

internal fun AppExtension.getProductFlavor(variant: BaseVariant): ProductFlavor? = productFlavors.find {
    it.name == variant.flavorName
}

internal fun AppExtension.getOriginalBuildType(variant: BaseVariant): BuildType = buildTypes.first {
    it.name == variant.buildType.name
}