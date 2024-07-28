package com.grab.sizer.analyzer.model

import com.grab.sizer.analyzer.ReportItem
import com.grab.sizer.analyzer.TeamMapping
import com.grab.sizer.analyzer.toModules
import java.io.File

private const val BUILD_FOLDER_PATH = "/build/"

data class Team(
    val name: String,
    val modules: List<Module>
) {
    val resourcesDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.othersDownloadSize } }
    val classDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.classDownloadSize } }
    fun getDownloadSize(): Long = modules.sumOf { it.getDownloadSize() }
}

data class Module(
    val name: String,
    val contributors: List<Contributor>
) {
    val resourcesDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.othersDownloadSize } }
    val classDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.classDownloadSize } }

    fun getDownloadSize(): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + classDownloadSize
}

internal fun Set<Contributor>.toTeams(teamMapping: TeamMapping): List<Team> {
    val modules = toModules()
    return teamMapping.teamToModuleMap.mapValues { entry ->
        entry.value.mapNotNull { moduleName ->
            modules.find { it.name == moduleName }
        }
    }.map { Team(it.key, it.value) }
}

internal fun List<Team>.sort(): List<Team> {
    return sortedWith { o1, o2 ->
        val size1 = o1.getDownloadSize()
        val size2 = o2.getDownloadSize()
        if (size1 > size2) -1
        else if (size1 < size2) 1
        else 0
    }
}

internal fun Set<Contributor>.toMapOfModuleToContributors(): Map<String, List<Contributor>> {
    return asSequence()
        .map { contributor -> contributor.path to contributor }
        .map { entry ->
            val path = entry.first
            val moduleName = if (path.contains(BUILD_FOLDER_PATH)) {
                getModuleNameFromPath(path)
            } else {
                /**
                 * If the aar/jar file does not belong to a module, just get the file name instead
                 */
                getFileNameFromPath(path)
            }
            moduleName to entry.second
        }
        .groupBy { it.first }
        .mapValues { item ->
            item.value.map { it.second }
        }
}

private fun getModuleNameFromPath(path: String): String {
    val segments = path.removeRange(path.indexOf(BUILD_FOLDER_PATH), path.length).split("/")
    return segments[segments.size - 1]
}

private fun getFileNameFromPath(path: String): String {
    return path.substringAfterLast(File.separator).substringBeforeLast(".")
}

internal fun Module.toReportItem(moduleToTeamMap: Map<String, String>): ReportItem =
    ReportItem(
        name = name,
        id = name,
        owner = moduleToTeamMap[name],
        extraInfo = "Sum up all codebase for $name",
        totalDownloadSize = getDownloadSize(),
        classesDownloadSize = classDownloadSize,
        nativeLibDownloadSize = nativeLibDownloadSize,
        resourceDownloadSize = resourcesDownloadSize,
        assetDownloadSize = assetsDownloadSize,
        otherDownloadSize = othersDownloadSize
    )

