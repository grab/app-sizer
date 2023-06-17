package com.grab.plugin.size.dependencies

class DependencyGraph {
    private val adjacencyList = mutableMapOf<ArchiveDependency, MutableList<ArchiveDependency>>()

    fun addDependencies(from: ArchiveDependency, to: List<ArchiveDependency>) {
        adjacencyList[from] = to.toMutableList()
    }

    fun addDependency(from: ArchiveDependency, to: ArchiveDependency) {
        adjacencyList.getOrPut(from) { mutableListOf() }.add(to)
    }

    fun getDependenciesOf(module: ArchiveDependency): List<ArchiveDependency> {
        return adjacencyList[module] ?: emptyList()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Dependency Graph:\n")
        adjacencyList.forEach { (key, value) ->
            builder.append("$key -> ${value.joinToString(", ")}\n")
        }
        return builder.toString()
    }
}

interface ArchiveDependency {
    val name: String
    val pathToArtifact: String
}

data class ExternalDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency
data class ModuleDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency
data class AppDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency