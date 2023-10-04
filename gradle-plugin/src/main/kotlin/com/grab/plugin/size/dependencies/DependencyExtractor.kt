package com.grab.plugin.size.dependencies

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.utils.isAndroidApplication
import com.grab.plugin.size.utils.isAndroidLibrary
import com.grab.plugin.size.utils.isKotlinJvm
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.DefaultResolvedDependency
import org.gradle.api.internal.artifacts.DependencyGraphNodeResult
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.the
import java.util.*


interface DependencyExtractor {
    fun extract(): DependencyGraph
}

class DependencyExtractorImpl(
    private val appProject: Project,
    private val variant: BaseVariant,
) : DependencyExtractor {
    override fun extract(): DependencyGraph {
        val dependencyGraph = DependencyGraph()
        val queue: Queue<Project> = LinkedList()
        queue.add(appProject)
        while (queue.isNotEmpty()) {
            val project = queue.poll()
            val projectArchiveDependency = project.toArchiveDependency(variant)
            project.filteredConfigurations(variant)
                .flatMap { it.dependencies }
                .filterIsInstance<DefaultProjectDependency>()
                .map { it.dependencyProject }
                .forEach { dependencyProject ->
                    val archive = dependencyProject.toArchiveDependency(variant)
                    dependencyGraph.addDependency(projectArchiveDependency, archive)
                    queue.add(dependencyProject)
                }
            fetchExternalDependency(project, variant, dependencyGraph, projectArchiveDependency)
        }
        return dependencyGraph
    }

    private fun fetchExternalDependency(
        project: Project,
        variant: BaseVariant,
        dependencyGraph: DependencyGraph,
        root: ArchiveDependency
    ) {
        project.filteredConfigurations(variant)
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
                val directDep = resolvedDep.toArchiveDependency()
                dependencyGraph.addDependency(root, directDep)
                val transitiveQueue = LinkedList<DependencyGraphNodeResult>()
                transitiveQueue.add(resolvedDep)
                while (transitiveQueue.isNotEmpty()) {
                    val item = transitiveQueue.poll()
                    item.outgoingEdges.forEach {
                        val transitiveDep = it.toArchiveDependency()
                        dependencyGraph.addDependency(directDep, transitiveDep)
                    }
                }
            }
    }

    private fun Project.filteredConfigurations(variant: BaseVariant?): Sequence<Configuration> {
        return configurations
            .asSequence()
            .filter { !it.name.contains("classpath", true) && !it.name.contains("lint") }
            .filter { !it.name.contains("coreLibraryDesugaring") }
            .filter { !it.name.startsWith("_") }
            .filter { !it.name.contains("archives") }
            // Todo : ensure filter by artifact applied
//            .filter { if (variant != null) it.name.contains(variant.name, true) else true }
            .filter { it.isNotTest() }
    }
}

internal fun DependencyGraphNodeResult.toArchiveDependency(): ArchiveDependency = ExternalDependency(
    name = publicView.name,
    group = publicView.moduleGroup,
    version = publicView.moduleVersion,
    pathToArtifact = publicView.allModuleArtifacts.first().file.path
)

internal fun DefaultResolvedDependency.toArchiveDependency(): ArchiveDependency = ExternalDependency(
    name = name,
    group = moduleGroup,
    version = moduleVersion,
    pathToArtifact = allModuleArtifacts.first().file.path
)

internal fun Project.toArchiveDependency(variant: BaseVariant): ArchiveDependency {
    return when {
        isAndroidApplication -> {
            return AppDependency(
                name = name,
                pathToArtifact = variant.outputs.first().outputFile.absolutePath
            )
        }
        isAndroidLibrary -> {
            val extension = the<LibraryExtension>()
            val libraryVariant = extension.libraryVariants.find { libraryVariant ->
                libraryVariant.name == variant.name
            } ?: extension.libraryVariants.find { libraryVariant ->
                libraryVariant.buildType.name == variant.buildType.name
            }

            if (libraryVariant != null) {
                return ModuleDependency(
                    name = name,
                    pathToArtifact = libraryVariant.outputs.first().outputFile.path
                )
            } else throw IllegalArgumentException("Can not fetch the output for $name")
        }
        isKotlinJvm -> {
            val jarTask = tasks.findByName(JavaPlugin.JAR_TASK_NAME) as Jar
            return ModuleDependency(
                name = name,
                pathToArtifact = jarTask.archiveFile.get().asFile.absolutePath
            )
        }
        else -> {
            throw IllegalArgumentException("The $name is not an Android/Kotlin module")
        }
    }
}

internal fun Configuration.isNotTest() = !name.contains("test", true)

