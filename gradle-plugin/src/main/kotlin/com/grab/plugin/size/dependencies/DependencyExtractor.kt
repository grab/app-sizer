package com.grab.plugin.size.dependencies

import com.grab.plugin.size.AppSizeTaskScope
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.DefaultResolvedDependency
import org.gradle.api.internal.artifacts.DependencyGraphNodeResult
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import java.util.*
import javax.inject.Inject

interface DependencyExtractor {
    fun extract(): DependencyGraph
}


private const val INTERNAL_DEP_VERSION = "unspecified"
@AppSizeTaskScope
class DefaultDependencyExtractor @Inject constructor(
    private val appProject: Project,
    private val configurationExtractor: ConfigurationExtractor,
    private val archiveExtractor: ArchiveExtractor
) : DependencyExtractor {
    override fun extract(): DependencyGraph {
        val dependencyGraph = MutableDependencyGraph()
        val dependenciesCache = mutableMapOf<String, ArchiveDependency>()
        val externalHaveChecked = mutableListOf<String>()
        val queue: Queue<Project> = LinkedList<Project>().apply { add(appProject) }
        while (queue.isNotEmpty()) {
            val project = queue.poll()
            val projectArchive = archiveExtractor.extract(project)
            dependenciesCache[projectArchive.id] = projectArchive
            fetchInternalDependency(project, dependenciesCache, dependencyGraph, projectArchive, queue)
            if (!externalHaveChecked.contains(projectArchive.id)) {
                fetchExternalDependency(project, projectArchive, dependencyGraph, dependenciesCache)
                externalHaveChecked.add(projectArchive.id)
            }

        }
        return dependencyGraph
    }

    private fun fetchInternalDependency(
        project: Project,
        dependenciesCache: MutableMap<String, ArchiveDependency>,
        dependencyGraph: MutableDependencyGraph,
        projectArchive: ArchiveDependency,
        queue: Queue<Project>
    ) {
        configurationExtractor.runtimeConfigurations(project)
            .flatMap { it.dependencies }
            .filterIsInstance<DefaultProjectDependency>()
            .map { it.dependencyProject }
            .forEach { dependencyProject ->
                val archive = archiveExtractor.extract(dependencyProject)
                if (!dependenciesCache.contains(archive.id)) {
                    dependenciesCache[archive.id] = archive
                }
                dependencyGraph.addDependency(
                    projectArchive,
                    dependenciesCache.getValue(archive.id)
                )
                queue.add(dependencyProject)
            }
    }

    private fun fetchExternalDependency(
        project: Project,
        root: ArchiveDependency,
        dependencyGraph: MutableDependencyGraph,
        dependenciesCache: MutableMap<String, ArchiveDependency>
    ) {
        configurationExtractor.runtimeConfigurations(project)
            .filter { it.isCanBeResolved }
            .map { it.resolvedConfiguration }
            .flatMap {
                try {
                    it.firstLevelModuleDependencies
                } catch (e: ResolveException) {
                    emptySet<ResolvedDependency>()
                }
            }
            .filterIsInstance<DefaultResolvedDependency>()
            .forEach { resolvedDep ->
                if (resolvedDep.moduleVersion != INTERNAL_DEP_VERSION) {
                    val directDep = resolvedDep.toArchiveDependency()

                    if (!dependenciesCache.contains(directDep.id)) {
                        dependenciesCache[directDep.id] = directDep
                    }

                    dependencyGraph.addDependency(root, dependenciesCache.getValue(directDep.id))
                    val transitiveQueue = LinkedList<DependencyGraphNodeResult>()
                    transitiveQueue.add(resolvedDep)
                    while (transitiveQueue.isNotEmpty()) {
                        val item = transitiveQueue.poll()

                        item.outgoingEdges.forEach {
                            val transitiveDep = it.toArchiveDependency()
                            if (!dependenciesCache.contains(transitiveDep.id)) {
                                dependenciesCache[transitiveDep.id] = transitiveDep
                            }

                            dependencyGraph.addDependency(directDep, dependenciesCache.getValue(transitiveDep.id))
                        }
                    }
                }

            }
    }
}

private fun DependencyGraphNodeResult.toArchiveDependency(): ArchiveDependency = ExternalDependency(
    name = publicView.name,
    group = publicView.moduleGroup,
    version = publicView.moduleVersion,
    pathToArtifact = publicView.allModuleArtifacts.first().file.path
)

private fun DefaultResolvedDependency.toArchiveDependency(): ArchiveDependency = ExternalDependency(
    name = name,
    group = moduleGroup,
    version = moduleVersion,
    pathToArtifact = allModuleArtifacts.first().file.path
)

