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
            .flatMap { it.dependencies }
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
                    logger.log("Fetching firstLevelModuleDependencies having issue with $it")
                    emptySet<ResolvedDependency>()
                }
            }
            .filterIsInstance<DefaultResolvedDependency>()
            .forEach { resolvedDep ->
                if (resolvedDep.moduleVersion != INTERNAL_DEP_VERSION) {
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
    name = id.displayName,
    pathToArtifact = file.path
)

