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


import com.grab.plugin.sizer.dependencies.ArchiveDependency
import com.grab.plugin.sizer.dependencies.ArchiveDependencyManager
import com.grab.plugin.sizer.dependencies.VariantArtifacts
import com.grab.plugin.sizer.utils.capitalize
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Writes the list of [ArchiveDependency] contributing to a variant to a json file, which
 * [AppSizeAnalysisTask] consumes as the inventory of AAR/JAR files.
 *
 * The entries are resolved lazily from the variant's runtime classpath (see
 * [VariantArtifacts]); this task only serializes the result.
 */
internal abstract class GenerateArchivesListTask : DefaultTask() {

    @get:Input
    abstract val archiveDependencies: ListProperty<ArchiveDependency>

    @get:OutputFile
    abstract val archiveDepFile: RegularFileProperty

    init {
        group = "build"
        description = "Generates list of archive dependencies for app size analysis"
    }

    @TaskAction
    fun run() {
        ArchiveDependencyManager().writeToJsonFile(
            archiveDependencies.get().toHashSet(),
            archiveDepFile.get().asFile
        )
        logger.info("Successfully generated archive dependencies")
    }

    companion object {
        fun registerTask(
            project: Project,
            variantName: String,
            variantArtifacts: VariantArtifacts,
        ) = project.tasks.register(
            "generateArchiveDep${variantName.capitalize()}",
            GenerateArchivesListTask::class.java
        ) { task ->
            task.archiveDependencies.set(variantArtifacts.archiveDependencies)
            task.archiveDepFile.set(
                project.layout.buildDirectory.file("sizer/dep/$variantName/dependencies.json")
            )
        }
    }
}
