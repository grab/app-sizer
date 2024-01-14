package com.grab.plugin.sizer

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.grab.plugin.sizer.dependencies.AndroidAppSizeVariant
import com.grab.plugin.sizer.dependencies.VariantExtractor
import com.grab.plugin.sizer.utils.isAndroidApplication
import com.grab.plugin.sizer.utils.isAndroidLibrary
import com.grab.plugin.sizer.utils.isJava
import com.grab.plugin.sizer.utils.isKotlinJvm
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.the
import java.io.File

internal class TaskManager(
    private val project: Project,
    private val pluginExtension: AppSizePluginExtension
) {
    fun configTasks() {
        project.gradle.projectsEvaluated {
            project.subprojects.forEach { subProject ->
                if (subProject.isAndroidApplication) {
                    configAppSizeTask(subProject)
                }
            }
        }
    }


    private fun configAppSizeTask(project: Project) {
        val appExtension = project.the<AppExtension>()
        appExtension.applicationVariants.forEach { variant ->
            val appSizeTaskComponent = DaggerAppSizeTaskComponent.factory().create(
                project = project,
                variant = variant,
                flavorMatchingFallbacks = appExtension.getProductFlavor(variant)?.matchingFallbacks ?: emptyList(),
                buildTypeMatchingFallbacks = appExtension.getOriginalBuildType(variant).matchingFallbacks
            )
            val apkDirectory = File("${variant.outputs.first().outputFile.parent}/apks")
            val generateApkTask = GenerateApkTask.registerTask(
                project,
                pluginExtension,
                variant,
                apkDirectory
            )

            val appSizeAnalysisTask = AppSizeAnalysisTask.registerTask(
                project,
                pluginExtension,
                apkDirectory,
                appSizeTaskComponent,
            )
            appSizeAnalysisTask.dependsOn(generateApkTask)
            registerAppSizeTaskDep(project, appSizeTaskComponent, appSizeAnalysisTask)
        }
    }

    private fun AppExtension.getProductFlavor(variant: BaseVariant): ProductFlavor? = productFlavors.find {
        it.name == variant.flavorName
    }

    private fun AppExtension.getOriginalBuildType(variant: BaseVariant): BuildType = buildTypes.first {
        it.name == variant.buildType.name
    }

    private fun registerAppSizeTaskDep(
        project: Project,
        appSizeTaskComponent: AppSizeTaskComponent,
        appSizeTask: TaskProvider<AppSizeAnalysisTask>
    ) {
        val markAsChecked = mutableSetOf<String>()
        dfs(project, markAsChecked, appSizeTaskComponent, appSizeTask)
    }

    private fun dfs(
        project: Project,
        markAsChecked: MutableSet<String>,
        appSizeTaskComponent: AppSizeTaskComponent,
        appSizeTask: TaskProvider<AppSizeAnalysisTask>
    ) {
        if (markAsChecked.contains(project.path)) return
        markAsChecked.add(project.path)
        handleSubProject(project, appSizeTask, appSizeTaskComponent.variantExtractor())
        appSizeTaskComponent.configurationExtractor()
            .runtimeConfigurations(project)
            .flatMap { configuration ->
                configuration.dependencies.withType(DefaultProjectDependency::class.java)
            }.forEach {
                dfs(it.dependencyProject, markAsChecked, appSizeTaskComponent, appSizeTask)
            }
    }

    private fun handleSubProject(
        project: Project,
        task: TaskProvider<AppSizeAnalysisTask>,
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