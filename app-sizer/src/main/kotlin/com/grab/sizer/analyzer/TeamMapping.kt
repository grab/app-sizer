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

package com.grab.sizer.analyzer
import java.io.File


/**
 * An interface that represents a bi-directional mapping between modules and teams.
 * It allows the retrieval of the associated team for a given module and vice versa.
 */
interface TeamMapping {
    val teamToModuleMap: Map<String, List<String>>
    val moduleToTeamMap: Map<String, String>
}

class DummyTeamMapping : TeamMapping {
    override val teamToModuleMap: Map<String, List<String>> = emptyMap()
    override val moduleToTeamMap: Map<String, String> = emptyMap()
}


class YmlTeamMapping(
    private val ymlFile: File
) : TeamMapping {
    override val teamToModuleMap: Map<String, List<String>> by lazy {
        loadTeamToModuleMap()
            .mapValues { entry ->
                entry.value.map { it.trim(':') }
            }
    }
    override val moduleToTeamMap: Map<String, String> by lazy {
        mutableMapOf<String, String>().apply {
            teamToModuleMap.forEach { (team, modules) ->
                modules.forEach { put(it, team) }
            }
        }
    }

    private fun loadTeamToModuleMap(): Map<String, List<String>> {
        val lines = ymlFile.readLines()
        val result = mutableMapOf<String, MutableList<String>>()
        var currentTeam: String? = null

        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.isEmpty() || trimmedLine.startsWith("#") -> continue // Skip empty lines and comments
                trimmedLine.endsWith(":") -> {
                    currentTeam = trimmedLine.dropLast(1)
                    result[currentTeam] = mutableListOf()
                }
                trimmedLine.startsWith("- ") -> {
                    currentTeam?.let {
                        result[it]?.add(trimmedLine.removePrefix("- ").trim())
                    }
                }
            }
        }

        return result
    }

}
