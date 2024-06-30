package com.grab.plugin.sizer.tasks


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.dependencies.*
import com.grab.sizer.utils.log
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * This task is used to generate the list of the [com.grab.plugin.sizer.dependencies.ArchiveDependency] to a json file
 * The file will be consumed by the [AppSizeAnalysisTask] as the input for the list of aar/jar files
 * This task is currently non-cacheable
 */
internal abstract class GenerateArchivesListTask : DefaultTask() {
    @get:Input
    abstract val variantInput: Property<VariantInput>

    @get:Input
    abstract val flavorMatchingFallbacks: ListProperty<String>

    @get:Input
    abstract val buildTypeMatchingFallbacks: ListProperty<String>

    @get:Input
    abstract val enableMatchDebugVariant: Property<Boolean>

    @get:OutputFile
    abstract val archiveDepFile: RegularFileProperty

    init {
        /**
         * Todo: Update this task to make it cacheable
         * If there is any dependencies updated, the task cache should be invalidated
         */
        outputs.upToDateWhen { false } // Mark this task as non-cacheable task

        archiveDepFile.convention {
            project.layout.buildDirectory.file("sizer/dep/${variantInput.get().name}/dependencies.json").get().asFile
        }
    }

    @TaskAction
    fun run() {
        createDependenciesComponent().run {
            ArchiveDependencyManager().writeToJsonFile(
                dependencyExtractor().extract(),
                archiveDepFile.get().asFile
            )
            logger().log("It's successful to generate the dependencies json file")
        }

    }

    private fun createDependenciesComponent(): DependenciesComponent = DaggerDependenciesComponent.factory().create(
        project,
        variantInput.get(),
        flavorMatchingFallbacks.get(),
        buildTypeMatchingFallbacks.get(),
        enableMatchDebugVariant.get()
    )

    companion object {
        fun registerTask(
            project: Project,
            variant: BaseVariant,
            flavorMatchingFallbacks: List<String>,
            buildTypeMatchingFallbacks: List<String>,
            enableMatchDebugVariant: Boolean
        ): TaskProvider<GenerateArchivesListTask> {
            return project.tasks.register(
                "generateArchiveDep${variant.name.capitalize()}", GenerateArchivesListTask::class.java
            ) {
                this.variantInput.set(variant.toVariantInput())
                this.buildTypeMatchingFallbacks.set(buildTypeMatchingFallbacks)
                this.flavorMatchingFallbacks.set(flavorMatchingFallbacks)
                this.enableMatchDebugVariant.set(enableMatchDebugVariant)
            }
        }
    }
}