package com.grab.sizer.analyzer.model

import com.grab.sizer.analyzer.toModules
import com.grab.sizer.report.TeamMapping
import com.grab.sizer.report.ReportItem

data class Team(
    val name: String,
    val modules: List<Module>
) {
    val resourcesDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { modules.sumOf { contributor -> contributor.othersDownloadSize } }
    val classSize: Long by lazy { modules.sumOf { contributor -> contributor.classSize } }
    fun getDownloadSize(downloadSizeRatio: Double): Long = modules.sumOf { it.getDownloadSize(downloadSizeRatio) }
}

data class Module(
    val name: String,
    val contributors: List<Contributor>
) {
    val resourcesDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.othersDownloadSize } }
    val classSize: Long by lazy { contributors.sumOf { contributor -> contributor.classSize } }

    fun getClassDownloadSize(downloadSizeRatio: Double): Long = (classSize * downloadSizeRatio).toLong()

    fun getDownloadSize(downloadSizeRatio: Double): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + getClassDownloadSize(
            downloadSizeRatio
        )
}

internal fun Set<Contributor>.toTeams(teamMapping: TeamMapping): List<Team> {
    val modules = toModules()
    return teamMapping.teamToModuleMap.mapValues { entry ->
        entry.value.mapNotNull { moduleName ->
            modules.find { it.name == moduleName }
        }
    }.map { Team(it.key, it.value) }
}

internal fun List<Team>.sort(dexCompressedRatio: Double): List<Team> {
    return sortedWith { o1, o2 ->
        val size1 = o1.getDownloadSize(dexCompressedRatio)
        val size2 = o2.getDownloadSize(dexCompressedRatio)
        if (size1 > size2) -1
        else if (size1 < size2) 1
        else 0
    }
}

internal fun Set<Contributor>.moduleToContributors(): Map<String, List<Contributor>> {
    return asSequence()
        .map { it.path to it }
        .map {
            val segments = it.first.removeRange(it.first.indexOf("/build/"), it.first.length).split("/")
            val moduleName = segments[segments.size - 1]
            moduleName to it.second
        }
        .groupBy { it.first }
        .mapValues { item ->
            item.value.map { it.second }
        }
}

fun Module.toReportItem(dexCompressedRatio: Double, moduleToTeamMap: Map<String, String>): ReportItem =
    ReportItem(
        name = name,
        id = name,
        owner = moduleToTeamMap[name],
        extraInfo = "Sum up all codebase for $name",
        totalDownloadSize = getDownloadSize(dexCompressedRatio),
        classesSize = classSize,
        classesDownloadSize = getClassDownloadSize(dexCompressedRatio),
        nativeLibDownloadSize = nativeLibDownloadSize,
        resourceDownloadSize = resourcesDownloadSize,
        assetDownloadSize = assetsDownloadSize,
        otherDownloadSize = othersDownloadSize
    )

