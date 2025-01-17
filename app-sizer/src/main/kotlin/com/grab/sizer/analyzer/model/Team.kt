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
import com.grab.sizer.parser.BinaryFileInfo

data class Team(
    val name: String,
    val modules: List<Module>
) {
    val resourcesSize: Long by lazy { modules.sumOf { contributor -> contributor.resourcesSize } }
    val nativeLibSize: Long by lazy { modules.sumOf { contributor -> contributor.nativeLibSize } }
    val assetsSize: Long by lazy { modules.sumOf { contributor -> contributor.assetsSize } }
    val othersSize: Long by lazy { modules.sumOf { contributor -> contributor.othersSize } }
    val classSize: Long by lazy { modules.sumOf { contributor -> contributor.classSize } }
    fun getSize(): Long = modules.sumOf { it.getSize() }
}

data class Module(
    private val owner: BinaryFileInfo,
    val contributors: List<Contributor>
) {
    val tag: String
        get() = owner.tag
    val path: String
        get() = owner.path
    val resourcesSize: Long by lazy { contributors.sumOf { contributor -> contributor.resourcesSize } }
    val nativeLibSize: Long by lazy { contributors.sumOf { contributor -> contributor.nativeLibSize } }
    val assetsSize: Long by lazy { contributors.sumOf { contributor -> contributor.assetsSize } }
    val othersSize: Long by lazy { contributors.sumOf { contributor -> contributor.othersSize } }
    val classSize: Long by lazy { contributors.sumOf { contributor -> contributor.classSize } }

    fun getSize(): Long =
        resourcesSize + nativeLibSize + assetsSize + othersSize + classSize
}

internal fun Set<Contributor>.toTeams(teamMapping: TeamMapping): List<Team> {
    val modules = toModules()
    return teamMapping.teamToModuleMap.mapValues { teamToModule ->
        teamToModule.value.mapNotNull { moduleNameFromTeamMapping ->
            modules.find { module -> module.tag == moduleNameFromTeamMapping }
        }
    }.map { Team(it.key, it.value) }
}

internal fun List<Team>.sort(): List<Team> {
    return sortedWith { o1, o2 ->
        val size1 = o1.getSize()
        val size2 = o2.getSize()
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

internal fun Module.toReportItem(moduleToTeamMap: Map<String, String>): ReportItem =
    ReportItem(
        name = tag,
        id = tag,
        owner = moduleToTeamMap[tag],
        extraInfo = "Sum up all codebase for $tag",
        totalSize = getSize(),
        classesSize = classSize,
        nativeLibSize = nativeLibSize,
        resourceSize = resourcesSize,
        assetSize = assetsSize,
        otherSize = othersSize
    )

