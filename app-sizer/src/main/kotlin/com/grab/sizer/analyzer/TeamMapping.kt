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

import com.grab.sizer.analyzer.validation.TeamValidator
import com.grab.sizer.analyzer.validation.ValidationReporter
import com.grab.sizer.utils.Logger
import java.io.File

/**
 * An interface that represents a bi-directional mapping between modules and teams,
 * with support for library ownership mapping using Maven coordinates.
 * It allows the retrieval of the associated team for a given module or library.
 *
 * Also serves as a data provider for validation operations.
 */
interface TeamMapping {
    // Core ownership lookup methods
    fun getModuleOwner(moduleName: String): String?
    fun getLibraryOwner(libraryCoordinate: String): String?
    fun getAllTeams(): Set<String>

    fun getModuleTeams(): Set<String>
    fun getLibraryTeams(): Set<String>

    companion object {
        /**
         * Factory method that creates a TeamMapping with automatic validation and reporting.
         * Encapsulates the orchestration of data loading, validation, and reporting.
         */
        fun createWithValidation(
            moduleFile: File,
            libraryFile: File? = null,
            logger: Logger
        ): TeamMapping = YmlTeamMapping(moduleFile, libraryFile).apply {
            ValidationReporter(logger).reportValidationResult(
                TeamValidator().validateTeamMapping(this)
            )
        }

    }
}

/**
 * Pure data provider implementation of TeamMapping.
 * Focused solely on loading and providing team data from YAML files.
 * No validation concerns - that's handled by TeamValidator.
 */
class YmlTeamMapping(
    private val moduleFile: File,
    private val libraryFile: File? = null
) : TeamMapping {

    private val teamToModuleMap: Map<String, List<String>> by lazy {
        loadTeamToModuleMap(moduleFile)
            .mapValues { entry ->
                entry.value.map { it.trim(':') }
            }
    }

    private val moduleToTeamMap: Map<String, String> by lazy {
        mutableMapOf<String, String>().apply {
            teamToModuleMap.forEach { (team, modules) ->
                modules.forEach { put(it, team) }
            }
        }
    }

    private val teamToLibraryMap: Map<String, List<String>> by lazy {
        if (libraryFile?.exists() == true) loadTeamToModuleMap(libraryFile)
        else emptyMap()
    }

    override fun getModuleOwner(moduleName: String): String? {
        return moduleToTeamMap[moduleName]
    }

    override fun getLibraryOwner(libraryCoordinate: String): String? {
        return findBestPatternMatch(libraryCoordinate, teamToLibraryMap)
    }

    override fun getAllTeams(): Set<String> {
        return (teamToModuleMap.keys + teamToLibraryMap.keys).toSet()
    }

    override fun getModuleTeams(): Set<String> {
        return teamToModuleMap.keys
    }

    override fun getLibraryTeams(): Set<String> {
        return teamToLibraryMap.keys
    }


    private fun findBestPatternMatch(coordinate: String, teamMapping: Map<String, List<String>>): String? {
        // Priority: exact > artifact wildcard > group wildcard
        for ((team, patterns) in teamMapping) {
            // Check exact match first
            if (coordinate in patterns) return team

            // Check artifact wildcards (com.example:library:*)
            patterns.filter { it.contains(":*") }.forEach { pattern ->
                val prefix = pattern.substringBeforeLast(":*")
                if (coordinate.startsWith(prefix)) return team
            }

            // Check group wildcards (com.example.*)
            patterns.filter { it.endsWith(".*") }.forEach { pattern ->
                val prefix = pattern.substringBeforeLast(".*")
                if (coordinate.startsWith(prefix)) return team
            }
        }
        return null
    }

    private fun loadTeamToModuleMap(file: File): Map<String, List<String>> {
        val lines = file.readLines()
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
