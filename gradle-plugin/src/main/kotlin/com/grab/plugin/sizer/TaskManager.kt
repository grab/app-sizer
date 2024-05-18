package com.grab.plugin.sizer

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.grab.plugin.sizer.configuration.DefaultVariantFilter
import com.grab.plugin.sizer.dependencies.AndroidAppSizeVariant
import com.grab.plugin.sizer.dependencies.DaggerDependenciesComponent
import com.grab.plugin.sizer.dependencies.DependenciesComponent
import com.grab.plugin.sizer.dependencies.VariantExtractor
import com.grab.plugin.sizer.tasks.AppSizeAnalysisTask
import com.grab.plugin.sizer.tasks.GenerateApkTask
import com.grab.plugin.sizer.tasks.GenerateArchivesListTask
import com.grab.plugin.sizer.utils.isAndroidApplication
import com.grab.plugin.sizer.utils.isAndroidLibrary
import com.grab.plugin.sizer.utils.isJava
import com.grab.plugin.sizer.utils.isKotlinJvm
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskProvider
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
        if (pluginExtension.enabled) {
            project.rootProject.gradle.projectsEvaluated {
                if (project.isAndroidApplication) {
                    configAppSizeTask(project)
                }
            }
        }
    }

    private fun configAppSizeTask(project: Project) {
        with(project.the<AppExtension>()) {
            applicationVariants.forEach { variant ->
                val variantFilter = DefaultVariantFilter(variant)
                pluginExtension.input.variantFilter?.execute(variantFilter)
                if (!variantFilter.ignored) {
                    val generateApkTask = GenerateApkTask.registerTask(
                        project,
                        pluginExtension,
                        variant
                    )

                    val generateArchivesListTask = GenerateArchivesListTask.registerTask(
                        project,
                        variant = variant,
                        flavorMatchingFallbacks = getProductFlavor(variant)?.matchingFallbacks ?: emptyList(),
                        buildTypeMatchingFallbacks = getOriginalBuildType(variant).matchingFallbacks,
                        enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant
                    )


                    val appSizeAnalysisTask = AppSizeAnalysisTask.registerTask(
                        project,
                        variant,
                        pluginExtension,
                        generateApkTask,
                        generateArchivesListTask,
                    )
                    registerAppSizeTaskDep(project, variant, this, appSizeAnalysisTask)
                }
            }
        }
    }

    private fun registerAppSizeTaskDep(
        project: Project,
        variant: BaseVariant,
        appExtension: AppExtension,
        appSizeTask: TaskProvider<out Task>
    ) {
        val dependenciesComponent = DaggerDependenciesComponent.factory().create(
            project = project,
            variant = variant,
            flavorMatchingFallbacks = appExtension.getProductFlavor(variant)?.matchingFallbacks ?: emptyList(),
            buildTypeMatchingFallbacks = appExtension.getOriginalBuildType(variant).matchingFallbacks,
            enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant
        )
        val markAsChecked = mutableSetOf<String>()
        dfs(project, markAsChecked, dependenciesComponent, appSizeTask)
    }

    private fun dfs(
        project: Project,
        markAsChecked: MutableSet<String>,
        dependenciesComponent: DependenciesComponent,
        appSizeTask: TaskProvider<out Task>
    ) {
        if (markAsChecked.contains(project.path)) return
        markAsChecked.add(project.path)
        handleSubProject(project, appSizeTask, dependenciesComponent.variantExtractor())
        dependenciesComponent.configurationExtractor()
            .runtimeConfigurations(project)
            .flatMap { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java)
            }.forEach {
                dfs(it.dependencyProject, markAsChecked, dependenciesComponent, appSizeTask)
            }
    }

    private fun handleSubProject(
        project: Project,
        task: TaskProvider<out Task>,
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
                task.dependsOn(project.tasks.named("assemble"))
            }

            project.isJava -> {
                task.dependsOn(project.tasks.named("assemble"))
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