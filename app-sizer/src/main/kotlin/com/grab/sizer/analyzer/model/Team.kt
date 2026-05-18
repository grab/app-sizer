/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.sizer.analyzer.model

import com.grab.sizer.analyzer.ReportItem
import com.grab.sizer.analyzer.TeamMapping
import com.grab.sizer.analyzer.NOT_AVAILABLE_VALUE
import com.grab.sizer.parser.BinaryFileInfo

data class Team(
    val name: String,
    val moduleContributors: List<Contributor>,
    val libContributors: List<Contributor>
) {

    val contributors: List<Contributor> = moduleContributors + libContributors

    fun getDownloadSize(): Long = contributors.sumOf { it.getDownloadSize() }

    val codebaseResourcesDownloadSize: Long by lazy { moduleContributors.sumOf { it.resourcesDownloadSize } }
    val codebaseNativeLibDownloadSize: Long by lazy { moduleContributors.sumOf { it.nativeLibDownloadSize } }
    val codebaseAssetsDownloadSize: Long by lazy { moduleContributors.sumOf { it.assetsDownloadSize } }
    val codebaseOthersDownloadSize: Long by lazy { moduleContributors.sumOf { it.othersDownloadSize } }
    val codebaseClassDownloadSize: Long by lazy { moduleContributors.sumOf { it.classDownloadSize } }

    // Library-specific calculations for library reports
    val libTotalDownloadSize: Long by lazy { libContributors.sumOf { it.getDownloadSize() } }
    val libNativeDownloadSize: Long by lazy { libContributors.sumOf { it.nativeLibDownloadSize } }

    val resourcesDownloadSize: Long by lazy { contributors.sumOf { it.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { contributors.sumOf { it.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { contributors.sumOf { it.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { contributors.sumOf { it.othersDownloadSize } }
    val classDownloadSize: Long by lazy { contributors.sumOf { it.classDownloadSize } }
}

data class Module(
    private val owner: BinaryFileInfo,
    val contributors: List<Contributor>
) {
    val tag: String
        get() = owner.tag
    val path: String
        get() = owner.path
    val resourcesDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.othersDownloadSize } }
    val classDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.classDownloadSize } }

    fun getDownloadSize(): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + classDownloadSize
}

internal fun Set<Contributor>.toTeams(
    teamMapping: TeamMapping?,
    includeUnowned: Boolean = false,
    moduleContributorPaths: Set<String> = emptySet(),
    libraryContributorPaths: Set<String> = emptySet()
): List<Team> {
    if (teamMapping == null && !includeUnowned) return emptyList()

    val moduleContributorsByTeam = mutableMapOf<String, MutableList<Contributor>>()
    val libContributorsByTeam = mutableMapOf<String, MutableList<Contributor>>()

    fun MutableMap<String, MutableList<Contributor>>.addContributor(teamName: String, contributor: Contributor) {
        getOrPut(teamName) { mutableListOf() }.add(contributor)
    }

    this.forEach { contributor ->
        val isModuleContributor = contributor.path in moduleContributorPaths
        val isLibraryContributor = contributor.path in libraryContributorPaths

        when {
            isModuleContributor -> {
                val teamName = teamMapping?.getModuleOwner(contributor.tag)
                    ?: NOT_AVAILABLE_VALUE.takeIf { includeUnowned }
                teamName?.let { moduleContributorsByTeam.addContributor(it, contributor) }
            }

            isLibraryContributor -> {
                val teamName = teamMapping?.getLibraryOwner(contributor.tag)
                    ?: NOT_AVAILABLE_VALUE.takeIf { includeUnowned }
                teamName?.let { libContributorsByTeam.addContributor(it, contributor) }
            }

            else -> {
                val moduleOwner = teamMapping?.getModuleOwner(contributor.tag)
                val libraryOwner = teamMapping?.getLibraryOwner(contributor.tag)
                moduleOwner?.let { moduleContributorsByTeam.addContributor(it, contributor) }
                libraryOwner?.let { libContributorsByTeam.addContributor(it, contributor) }
                if (moduleOwner == null && libraryOwner == null && includeUnowned) {
                    moduleContributorsByTeam.addContributor(NOT_AVAILABLE_VALUE, contributor)
                }
            }
        }
    }

    val allTeamNames = moduleContributorsByTeam.keys + libContributorsByTeam.keys
    return allTeamNames.map { teamName ->
        Team(
            teamName,
            moduleContributorsByTeam[teamName] ?: emptyList(),
            libContributorsByTeam[teamName] ?: emptyList()
        )
    }
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

internal fun Set<Contributor>.toModules(): List<Module> {
    return asSequence()
        .map { contributor -> contributor.originalOwner to contributor }
        .groupBy { it.first }
        .mapValues { item ->
            item.value.map { it.second }
        }.map {
            Module(it.key, it.value)
        }
}

internal fun Module.toReportItem(teamMapping: TeamMapping?): ReportItem =
    ReportItem(
        name = tag,
        id = tag,
        owner = teamMapping?.getModuleOwner(tag),
        extraInfo = "Sum up all codebase for $tag",
        totalDownloadSize = getDownloadSize(),
        classesDownloadSize = classDownloadSize,
        nativeLibDownloadSize = nativeLibDownloadSize,
        resourceDownloadSize = resourcesDownloadSize,
        assetDownloadSize = assetsDownloadSize,
        otherDownloadSize = othersDownloadSize
    )
