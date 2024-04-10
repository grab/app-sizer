package com.grab.plugin.sizer.dependencies

import com.grab.plugin.sizer.AppSizeTaskScope
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.log
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
    private val archiveExtractor: ArchiveExtractor,
    private val logger: Logger
) : DependencyExtractor {
    override fun extract(): DependencyGraph {
        val dependencyGraph = MutableDependencyGraph()
        val checkedProjects = mutableSetOf<String>()
        val queue: Queue<Project> = LinkedList<Project>().apply { add(appProject) }

        while (queue.isNotEmpty()) {
            val project = queue.poll()
            val projectArchive = archiveExtractor.extract(project)
            fetchInternalDependency(project, dependencyGraph, projectArchive, checkedProjects, queue)
            fetchExternalDependency(project, projectArchive, dependencyGraph, checkedProjects)
        }
        return dependencyGraph
    }

    private fun fetchInternalDependency(
        project: Project,
        dependencyGraph: MutableDependencyGraph,
        projectArchive: ArchiveDependency,
        checkedProjects : MutableSet<String>,
        queue: Queue<Project>
    ) {
        configurationExtractor.runtimeConfigurations(project)
            .flatMap { it.dependencies }
            .filterIsInstance<DefaultProjectDependency>()
            .map { it.dependencyProject }
            .forEach { dependencyProject ->
                dependencyGraph.addDependency(
                    projectArchive,
                    archiveExtractor.extract(dependencyProject)
                )
                if(!checkedProjects.contains(dependencyProject.path)){
                    queue.add(dependencyProject)
                    checkedProjects.add(dependencyProject.path)
                }
            }
    }

    private fun fetchExternalDependency(
        project: Project,
        projectArchive: ArchiveDependency,
        dependencyGraph: MutableDependencyGraph,
        checkedProjects : MutableSet<String>,
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
                    val archiveResolvedDep = resolvedDep.toArchiveDependency()
                    dependencyGraph.addDependency(projectArchive, archiveResolvedDep)

                    //if the lib haven't fetched the transitive dep
                    if(!checkedProjects.contains(resolvedDep.name)){
                        checkedProjects.add(resolvedDep.name)
                        val transitiveQueue = LinkedList<DependencyGraphNodeResult>()
                        transitiveQueue.add(resolvedDep)
                        while (transitiveQueue.isNotEmpty()) {
                            val item = transitiveQueue.poll()

                            item.outgoingEdges.forEach {
                                logger.log("Fetch dependency ${it.publicView.name}")
                                // Todo - check bom file dependencies "org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"
                                if (it.publicView.allModuleArtifacts.isNotEmpty()) {
                                    dependencyGraph.addDependency(archiveResolvedDep, it.toArchiveDependency())
                                }
                            }
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

