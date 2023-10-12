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

    fun getAll(): Sequence<ArchiveDependency> = adjacencyList.values.asSequence().flatMap { it }

    override fun toString(): String {
        return StringBuilder().apply {
            append("Dependency Graph:\n")
            adjacencyList.forEach { (key, value) ->
                append("$key -> ${value.joinToString(", ")}\n")
            }
        }.toString()
    }
}