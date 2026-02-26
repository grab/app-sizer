/*
 * MIT License
 *
 * Copyright (c) 2026.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
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

package com.grab.sizer.analyzer.validation

import com.grab.sizer.analyzer.TeamMapping

/**
 * Pure business logic class for validating team consistency.
 * Consumes TeamMapping as a data provider - clean inversion of control.
 * Contains no side effects - only returns structured validation results.
 */
class TeamValidator {

    /**
     * Validates team consistency using TeamMapping as data provider.
     * This is the primary validation entry point that uses inversion of control.
     *
     * @param teamMapping Data provider for team information
     * @return TeamValidationResult with structured validation results
     */
    fun validateTeamMapping(teamMapping: TeamMapping): TeamValidationResult {
        val libraryTeams = teamMapping.getLibraryTeams()

        // Skip validation if no libraries are configured
        if (libraryTeams.isEmpty()) {
            return TeamValidationResult.Success("No libraries configured for validation")
        }

        return validateTeamConsistency(
            moduleTeams = teamMapping.getModuleTeams(),
            libraryTeams = libraryTeams
        )
    }

    /**
     * Validates team consistency between module and library mappings.
     * Lower-level method for direct testing of business logic.
     *
     * @param moduleTeams Set of team names from module mapping
     * @param libraryTeams Set of team names from library mapping
     * @return TeamValidationResult with structured validation results
     */
    fun validateTeamConsistency(
        moduleTeams: Set<String>,
        libraryTeams: Set<String>
    ): TeamValidationResult {
        val issues = mutableListOf<ValidationIssue>()

        // Check for teams that exist in one mapping but not the other
        val onlyInModules = moduleTeams - libraryTeams
        val onlyInLibraries = libraryTeams - moduleTeams

        if (onlyInModules.isNotEmpty()) {
            issues.add(
                ValidationIssue.TeamsOnlyInModules(teams = onlyInModules)
            )
        }

        if (onlyInLibraries.isNotEmpty()) {
            issues.add(
                ValidationIssue.TeamsOnlyInLibraries(teams = onlyInLibraries)
            )

            // Check for similar team names (potential typos) - only for library-only teams
            // This helps identify if a library-only team might be a typo of an existing module team
            val similarTeams = findSimilarLibraryOnlyTeams(onlyInLibraries, moduleTeams)
            if (similarTeams.isNotEmpty()) {
                issues.add(ValidationIssue.SimilarTeamNames(similarTeams))
            }
        }

        return if (issues.isEmpty()) {
            TeamValidationResult.Success()
        } else {
            TeamValidationResult.Warnings(issues)
        }
    }

    /**
     * Finds library-only teams that have similar names to existing module teams (potential typos).
     * Optimized with early termination and length-based filtering.
     */
    private fun findSimilarLibraryOnlyTeams(
        libraryOnlyTeams: Set<String>,
        moduleTeams: Set<String>
    ): List<TeamSimilarity> {
        val similarPairs = mutableListOf<TeamSimilarity>()
        val threshold = 0.7

        for (libraryTeam in libraryOnlyTeams) {
            var bestMatch: TeamSimilarity? = null

            for (moduleTeam in moduleTeams) {
                // Skip if length difference is too large for 70% similarity
                val lengthDiff = kotlin.math.abs(libraryTeam.length - moduleTeam.length)
                val maxLength = kotlin.math.max(libraryTeam.length, moduleTeam.length)
                if (lengthDiff.toDouble() / maxLength > (1 - threshold)) continue

                val similarity = calculateStringSimilarity(libraryTeam, moduleTeam)

                // Consider teams similar if similarity > 0.7 (70%)
                if (similarity > threshold) {
                    val candidate = TeamSimilarity(
                        libraryTeam = libraryTeam,
                        moduleTeam = moduleTeam,
                        similarity = similarity
                    )
                    // Keep only the best match for each library team
                    if (bestMatch == null || similarity > bestMatch.similarity) {
                        bestMatch = candidate
                    }
                }
            }

            bestMatch?.let { similarPairs.add(it) }
        }

        return similarPairs
    }

    /**
     * Calculates string similarity using normalized Levenshtein distance.
     *
     * @return A value between 0.0 (no similarity) and 1.0 (identical strings)
     */
    private fun calculateStringSimilarity(str1: String, str2: String): Double {
        val longer = if (str1.length > str2.length) str1.lowercase() else str2.lowercase()
        val shorter = if (str1.length > str2.length) str2.lowercase() else str1.lowercase()

        if (longer.isEmpty()) return 1.0

        // Use Levenshtein distance for similarity calculation
        val editDistance = levenshteinDistance(longer, shorter)
        return (longer.length - editDistance) / longer.length.toDouble()
    }

    /**
     * Calculates the Levenshtein distance between two strings with memory bounds.
     * The Levenshtein distance is the minimum number of single-character edits
     * (insertions, deletions, or substitutions) required to change one word into another.
     *
     * @param str1 First string
     * @param str2 Second string
     * @return The Levenshtein distance as an integer
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        // Limit memory allocation for very long strings
        val maxLen = 100
        val s1 = if (str1.length > maxLen) str1.take(maxLen) else str1
        val s2 = if (str2.length > maxLen) str2.take(maxLen) else str2

        // Use space-optimized version for large strings (O(min(m,n)) space instead of O(m*n))
        if (s1.length > s2.length) return levenshteinDistance(s2, s1)

        var previousRow = IntArray(s1.length + 1) { it }
        var currentRow = IntArray(s1.length + 1)

        for (i in 1..s2.length) {
            currentRow[0] = i
            for (j in 1..s1.length) {
                val cost = if (s2[i - 1] == s1[j - 1]) 0 else 1
                currentRow[j] = minOf(
                    previousRow[j] + 1,        // deletion
                    currentRow[j - 1] + 1,     // insertion
                    previousRow[j - 1] + cost  // substitution
                )
            }
            // Swap rows
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }

        return previousRow[s1.length]
    }
}
