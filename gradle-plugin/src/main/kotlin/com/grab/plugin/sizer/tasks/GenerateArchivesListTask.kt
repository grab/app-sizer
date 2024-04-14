package com.grab.plugin.sizer.tasks


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.dependencies.DaggerDependenciesComponent
import com.grab.plugin.sizer.dependencies.DependenciesComponent
import com.grab.sizer.utils.log
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * This task is used to generate the list of the [com.grab.plugin.sizer.dependencies.ArchiveDependency] to a json file
 * The file will be consumed by the [AppSizeAnalysisTask] as the input for the list of aar/jar files
 */
internal abstract class GenerateArchivesListTask : DefaultTask() {
    @get:Internal
    abstract val variant: Property<BaseVariant>

    @get:Input
    abstract val flavorMatchingFallbacks: ListProperty<String>

    @get:Input
    abstract val buildTypeMatchingFallbacks: ListProperty<String>

    @get:OutputFile
    abstract val archiveDepFile: RegularFileProperty

    init {
        archiveDepFile.convention(project.layout.buildDirectory.file("sizer/dep/dependencies.json"))
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
        variant.get(),
        flavorMatchingFallbacks.get(),
        buildTypeMatchingFallbacks.get()
    )

    companion object {
        fun registerTask(
            project: Project,
            variant: BaseVariant,
            flavorMatchingFallbacks: List<String>,
            buildTypeMatchingFallbacks: List<String>
        ): TaskProvider<GenerateArchivesListTask> {
            return project.tasks.register(
                "generateArchiveDep${variant.name.capitalize()}", GenerateArchivesListTask::class.java
            ) {
                this.variant.set(variant)
                this.buildTypeMatchingFallbacks.set(buildTypeMatchingFallbacks)
                this.flavorMatchingFallbacks.set(flavorMatchingFallbacks)
            }
        }
    }
}