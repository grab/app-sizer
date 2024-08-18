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

    /**
     * This is a workaround, by default, this task haven't support catching yet
     * This flag to force the task become cacheable by default. It's not recommend to enable this flag
     */
    @get:Input
    abstract val archiveDepTaskCacheable: Property<Boolean>


    @get:OutputFile
    abstract val archiveDepFile: RegularFileProperty

    init {
        /**
         * Todo: Update this task to make it cacheable
         * If there is any dependencies updated, the task cache should be invalidated
         */
        outputs.upToDateWhen { archiveDepTaskCacheable.get() } // Mark this task as non-cacheable task

        archiveDepFile.convention {
            project.layout.buildDirectory.file("sizer/dep/${variantInput.get().name}/dependencies.json").get().asFile
        }
    }

    @TaskAction
    fun run() {
        if(enableMatchDebugVariant.get()){
            /**
             * Extracts and manages project dependencies, separating modules from external libraries.
             *
             * This code performs the following steps:
             * 1. Extracts module dependencies:
             *    - Uses createDependenciesComponent(true) to enable matching debug variants.
             *    - This is a workaround for cases where modules cannot be compiled in release build type.
             *    - When enabled, it fetches module AAR/JAR files from the debug variant.
             * 2. Extracts library dependencies:
             *    - Uses createDependenciesComponent(false) to fetch libraries from the input variant.
             * 3. Combines and processes dependencies:
             *    - Filters out external dependencies from modules.
             *    - Filters to include only external dependencies for libraries.
             *
             * This approach ensures proper handling of both module and external library dependencies,
             * accommodating potential build type incompatibilities.
             */
            val modules = createDependenciesComponent(true)
                .dependencyExtractor()
                .extract()
                .filter { it !is ExternalDependency }
            val libraries = createDependenciesComponent(false)
                .dependencyExtractor()
                .extract()
                .filterIsInstance<ExternalDependency>()

            ArchiveDependencyManager().writeToJsonFile(
                (modules + libraries).toHashSet(),
                archiveDepFile.get().asFile
            )
        }else{
            createDependenciesComponent(false).run {
                ArchiveDependencyManager().writeToJsonFile(
                    dependencyExtractor().extract(),
                    archiveDepFile.get().asFile
                )
            }
        }

    }

    private fun createDependenciesComponent(enableMatchDebugVariant : Boolean): DependenciesComponent = DaggerDependenciesComponent.factory().create(
        project,
        variantInput.get(),
        flavorMatchingFallbacks.get(),
        buildTypeMatchingFallbacks.get(),
        enableMatchDebugVariant
    )

    companion object {
        fun registerTask(
            project: Project,
            variant: BaseVariant,
            flavorMatchingFallbacks: List<String>,
            buildTypeMatchingFallbacks: List<String>,
            enableMatchDebugVariant: Boolean,
            archiveDepTaskCacheable : Boolean
        ): TaskProvider<GenerateArchivesListTask> {
            return project.tasks.register(
                "generateArchiveDep${variant.name.capitalize()}", GenerateArchivesListTask::class.java
            ) {
                this.variantInput.set(variant.toVariantInput())
                this.buildTypeMatchingFallbacks.set(buildTypeMatchingFallbacks)
                this.flavorMatchingFallbacks.set(flavorMatchingFallbacks)
                this.enableMatchDebugVariant.set(enableMatchDebugVariant)
                this.archiveDepTaskCacheable.set(archiveDepTaskCacheable)
            }
        }
    }
}