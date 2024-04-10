package com.grab.plugin.sizer.dependencies

//interface ArchiveDependencyStore {
//    fun getAll(): Sequence<ArchiveDependency>
//    fun getDependenciesOf(module: ArchiveDependency): Set<ArchiveDependency>
//}
//
///**
// * This class is a adjacency list of a graph.
// */
//class MutableArchiveDependencyStore : ArchiveDependencyStore {
////    private val adjacencyList = mutableMapOf<ArchiveDependency, MutableSet<ArchiveDependency>>()
//    private val nodeCache = mutableMapOf<String, ArchiveDependency>()
//
//    fun addDependency(from: ArchiveDependency, to: ArchiveDependency) {
//        val noDuplicateFrom = nodeCache[from.id] ?: from
//        val noDuplicateTo = nodeCache[to.id] ?: to
////        adjacencyList.getOrPut(noDuplicateFrom) { mutableSetOf() }.add(noDuplicateTo)
//        nodeCache[from.id] = noDuplicateFrom
//        nodeCache[to.id] = noDuplicateTo
//        println("Debug nodeCache ${nodeCache.size}")
////        println("Debug adjacencyList ${adjacencyList.len}")
//    }
//
//    override fun getDependenciesOf(module: ArchiveDependency): Set<ArchiveDependency> {
//        return emptySet()
//    }
//
//    override fun getAll(): Sequence<ArchiveDependency> = nodeCache.values.asSequence()
//}