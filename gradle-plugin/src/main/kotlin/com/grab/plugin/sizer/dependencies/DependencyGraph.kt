package com.grab.plugin.sizer.dependencies

interface DependencyGraph {
    fun getAll(): Sequence<ArchiveDependency>
    fun getDependenciesOf(module: ArchiveDependency): Set<ArchiveDependency>
}

/**
 * This class is a adjacency list of a graph.
 */
class MutableDependencyGraph : DependencyGraph {
    private val adjacencyList = mutableMapOf<ArchiveDependency, MutableList<ArchiveDependency>>()
    private val nodeCache = mutableMapOf<String, ArchiveDependency>()

    fun addDependency(from: ArchiveDependency, to: ArchiveDependency) {
        val noDuplicateFrom = nodeCache[from.id] ?: from
        val noDuplicateTo = nodeCache[to.id] ?: to
        adjacencyList.getOrPut(noDuplicateFrom) { mutableListOf() }.add(noDuplicateTo)
        nodeCache[from.id] = noDuplicateFrom
        nodeCache[to.id] = noDuplicateTo
    }

    override fun getDependenciesOf(module: ArchiveDependency): Set<ArchiveDependency> {
        return adjacencyList[module]?.toSet() ?: emptySet()
    }

    override fun getAll(): Sequence<ArchiveDependency> = nodeCache.values.asSequence()

    override fun toString(): String {
        return StringBuilder().apply {
            append("Dependency Graph:\n")
            adjacencyList.forEach { (key, value) ->
                append("$key -> ${value.joinToString(", ")}\n")
            }
        }.toString()
    }
}