package com.grab.plugin.size.dependencies

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

internal fun Project.filteredConfigurations(variant: BaseVariant?): Sequence<Configuration> {
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

fun projectsDependenciesSet(project: Project, variant: BaseVariant?): Set<Project> {
    val markAsChecked = mutableSetOf<String>()
    return dfs(project, markAsChecked, variant)
}

private fun dfs(project: Project, markAsChecked: MutableSet<String>, variant: BaseVariant?): Set<Project> {
    if (markAsChecked.contains(project.path)) return emptySet()
    markAsChecked.add(project.path)
    return setOf(project) + project.filteredConfigurations(variant)
        .flatMap { configuration ->
            configuration.dependencies.withType(DefaultProjectDependency::class.java)
        }
        .map { it.dependencyProject }
        .flatMap { subProject -> dfs(subProject, markAsChecked, variant) }
        .toSet()
}
