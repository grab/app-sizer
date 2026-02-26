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

import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.log

/**
 * Handles the conversion from structured validation results to user-facing log messages.
 * Maintains exact same logging format for backward compatibility.
 */
class ValidationReporter(
    private val logger: Logger
) {

    /**
     * Reports validation results to the logger, maintaining backward compatibility
     * with existing log message format.
     */
    fun reportValidationResult(result: TeamValidationResult) {
        when (result) {
            is TeamValidationResult.Success -> {
                // Skip verbose success messages to reduce noise
                // Only print if explicitly needed for debugging
            }
            is TeamValidationResult.Warnings -> {
                result.issues.forEach { issue ->
                    reportValidationIssue(issue)
                }
            }
        }
    }

    private fun reportValidationIssue(issue: ValidationIssue) {
        when (issue) {
            is ValidationIssue.TeamsOnlyInModules -> {
                // Skip for now - module ownership is the source of truth.
            }

            is ValidationIssue.TeamsOnlyInLibraries -> {
                logger.log("⚠️  WARNING: Library-only teams detected (teams that own libraries but no modules):")
                issue.teams.forEach { team ->
                    logger.log("   - '$team' (from libraries)")
                }
                logger.log("   This could indicate:")
                logger.log("   • Missing team definitions in modules")
                logger.log("   • Typos in team names between files")
                logger.log("   Review your team structure to ensure this is intentional.")
                logger.log("")
            }

            is ValidationIssue.SimilarTeamNames -> {
                logger.log("⚠️  WARNING: Library-only teams with similar names to existing module teams (potential typos):")
                issue.potentialTypos.forEach { similarity ->
                    logger.log("   - Library team: '${similarity.libraryTeam}' (from libraries)")
                    logger.log("   - Similar to module team: '${similarity.moduleTeam}' (from modules)")
                    logger.log("     Similarity: ${(similarity.similarity * 100).toInt()}% - Please verify if '${similarity.libraryTeam}' should be '${similarity.moduleTeam}'")
                    logger.log("")
                }
            }
        }
    }
}
