package com.grab.plugin.sizer.dependencies

interface DependencyGraph {
    fun getAll(): Sequence<ArchiveDependency>
    fun getDependenciesOf(module: ArchiveDependency): Set<ArchiveDependency>
}

class MutableDependencyGraph : DependencyGraph {
    private val adjacencyList = mutableMapOf<ArchiveDependency, MutableList<ArchiveDependency>>()

    fun setDependencies(from: ArchiveDependency, to: List<ArchiveDependency>) {
        adjacencyList[from] = to.toMutableList()
    }

    fun addDependency(from: ArchiveDependency, to: ArchiveDependency) {
        adjacencyList.getOrPut(from) { mutableListOf() }.add(to)
    }

    override fun getDependenciesOf(module: ArchiveDependency): Set<ArchiveDependency> {
        return adjacencyList[module]?.toSet() ?: emptySet()
    }

    override fun getAll(): Sequence<ArchiveDependency> = adjacencyList.values
        .asSequence()
        .flatMap { it }
        .distinct()

    override fun toString(): String {
        return StringBuilder().apply {
            append("Dependency Graph:\n")
            adjacencyList.forEach { (key, value) ->
                append("$key -> ${value.joinToString(", ")}\n")
            }
        }.toString()
    }
}