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

/**
 * Sealed class representing the result of team validation between module and library mappings.
 */
sealed class TeamValidationResult {
    /**
     * Represents successful validation with no issues found.
     */
    data class Success(val message: String = "Team configuration is consistent") : TeamValidationResult()

    /**
     * Represents validation that found issues but can still proceed.
     */
    data class Warnings(val issues: List<ValidationIssue>) : TeamValidationResult()
}

/**
 * Sealed class representing different types of validation issues.
 */
sealed class ValidationIssue {
    abstract val severity: ValidationSeverity
    abstract val message: String
    abstract val suggestion: String?

    /**
     * Teams that exist in module mapping but not in library mapping.
     */
    data class TeamsOnlyInModules(
        val teams: Set<String>
    ) : ValidationIssue() {
        override val severity: ValidationSeverity = ValidationSeverity.WARNING
        override val message: String = "Teams defined in module ownership but missing in library ownership: ${teams.joinToString(", ")}"
        override val suggestion: String = "Consider adding these teams to library ownership or removing unused teams."
    }

    /**
     * Teams that exist in library mapping but not in module mapping.
     */
    data class TeamsOnlyInLibraries(
        val teams: Set<String>
    ) : ValidationIssue() {
        override val severity: ValidationSeverity = ValidationSeverity.WARNING
        override val message: String = "Library-only teams detected (teams that own libraries but no modules): ${teams.joinToString(", ")}"
        override val suggestion: String = "This could indicate valid library-only teams (e.g., DevOps, Infrastructure teams), missing team definitions in module ownership, or typos in team names between mappings. Review your team structure to ensure this is intentional."
    }

    /**
     * Teams with similar names that might be typos.
     */
    data class SimilarTeamNames(
        val potentialTypos: List<TeamSimilarity>
    ) : ValidationIssue() {
        override val severity: ValidationSeverity = ValidationSeverity.WARNING
        override val message: String = "Library-only teams with similar names to existing module teams (potential typos)"
        override val suggestion: String = "Please verify if these are intentional team name variations or typos that should be corrected."
    }
}

/**
 * Represents a potential typo between library team name and module team name.
 */
data class TeamSimilarity(
    val libraryTeam: String,
    val moduleTeam: String,
    val similarity: Double
)

/**
 * Severity levels for validation issues.
 */
enum class ValidationSeverity {
    INFO, WARNING, ERROR
}
