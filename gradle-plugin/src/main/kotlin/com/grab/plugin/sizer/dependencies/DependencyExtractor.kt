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

package com.grab.plugin.sizer.dependencies

import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.log
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.DefaultResolvedDependency
import org.gradle.internal.component.AmbiguousVariantSelectionException
import java.util.*
import javax.inject.Inject

typealias ArchiveDependencyStore = HashSet<ArchiveDependency>

interface DependencyExtractor {
    fun extract(): ArchiveDependencyStore
}


private const val INTERNAL_DEP_VERSION = "unspecified"

@DependenciesScope
class DefaultDependencyExtractor @Inject constructor(
    private val appProject: Project,
    private val configurationExtractor: ConfigurationExtractor,
    private val archiveExtractor: ArchiveExtractor,
    private val logger: Logger
) : DependencyExtractor {
    override fun extract(): ArchiveDependencyStore {
        return ArchiveDependencyStore().apply {
            val checkedProjects = mutableSetOf<String>()
            val queue: Queue<Project> = LinkedList<Project>().apply { add(appProject) }

            while (queue.isNotEmpty()) {
                val project = queue.poll()
                val projectArchive = archiveExtractor.extract(project)
                add(projectArchive)
                fetchInternalDependency(project, this, checkedProjects, queue)
                fetchExternalDependency(project, this)
            }
        }
    }

    private fun fetchInternalDependency(
        project: Project,
        archiveDependencyStore: ArchiveDependencyStore,
        checkedProjects: MutableSet<String>,
        queue: Queue<Project>
    ) {
        configurationExtractor.runtimeConfigurations(project)
            .flatMap { configuration -> configuration.dependencies }
            .filterIsInstance<ProjectDependency>()
            .map { it.dependencyProject }
            .forEach { dependencyProject ->
                archiveDependencyStore.add(
                    archiveExtractor.extract(dependencyProject)
                )
                if (!checkedProjects.contains(dependencyProject.path)) {
                    queue.add(dependencyProject)
                    checkedProjects.add(dependencyProject.path)
                }
            }
    }

    private fun fetchExternalDependency(
        project: Project,
        archiveDependencyStore: ArchiveDependencyStore
    ) {
        configurationExtractor.runtimeConfigurations(project)
            .filter { it.isCanBeResolved }
            .map { it.resolvedConfiguration }
            .flatMap {
                try {
                    it.firstLevelModuleDependencies
                } catch (e: ResolveException) {
                    logger.log("Fetching firstLevelModuleDependencies having issue with $it for ${project.name}")
                    emptySet<ResolvedDependency>()
                }
            }
            .filterIsInstance<ResolvedDependency>()
            .forEach { resolvedDep ->
                /**
                 * Haven't found a proper way to detect if the resolvedDep is a module or a library
                 * Here is a workaround, it will not work if the library group starting with the root project name
                 */
                if (!resolvedDep.moduleGroup.startsWith(project.rootProject.name) && resolvedDep.moduleVersion != INTERNAL_DEP_VERSION) {
                    try {
                        resolvedDep.allModuleArtifacts.forEach { artifact ->
                            archiveDependencyStore.add(artifact.toArchiveDependency())
                        }
                    } catch (e: AmbiguousVariantSelectionException) {
                        logger.log("Fetching allModuleArtifacts having issue with ${resolvedDep.name}")
                    }
                }
            }
    }
}

private fun ResolvedArtifact.toArchiveDependency(): ArchiveDependency = ExternalDependency(
    name = id.componentIdentifier.toString(),
    pathToArtifact = file.path
)

