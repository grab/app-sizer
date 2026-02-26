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

import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.JarFileInfo
import com.grab.sizer.report.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TeamAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = TeamAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        teamMapping = project1Data.teamMapping
    )

    @Test
    fun testTeamAnalyzerWithProject1Data() {
        val report = analyzer.process().sort()

        // Verify report structure
        assertEquals("team", report.id)
        assertEquals("team", report.name)
        assertEquals(16, report.rows.size, "Should have 8 breakdown rows per team * 2 teams = 16 total")

        // Verify team1 total size (modules: 103 + libraries: libAar1=15 + libJar=7 = 125 total)
        val team1TotalRow = report.rows.find { row ->
            row.name == "team1" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team1TotalRow, "Should have team1 total row")
        assertEquals(125L, team1TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value)

        // Verify team2 total size (modules: 107 + libraries: libAar2=40 = 147 total)
        val team2TotalRow = report.rows.find { row ->
            row.name == "team2" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team2TotalRow, "Should have team2 total row")
        assertEquals(147L, team2TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value)

        // Verify all teams have complete breakdown rows
        val team1Rows = report.rows.filter { row ->
            val ownerField = row.fields.find { it.name == FIELD_KEY_OWNER }
            ownerField?.value == "team1"
        }
        assertEquals(8, team1Rows.size, "team1 should have 8 breakdown rows")

        val team2Rows = report.rows.filter { row ->
            val ownerField = row.fields.find { it.name == FIELD_KEY_OWNER }
            ownerField?.value == "team2"
        }
        assertEquals(8, team2Rows.size, "team2 should have 8 breakdown rows")
    }

    @Test
    fun testTeamAnalyzerShouldReportCorrectNumberOfRows() {
        val report = analyzer.process()
        // Each team should have 8 breakdown rows: total, android-java-libraries, native-libraries, codebase-kotlin-java, codebase-resources, codebase-assets, codebase-native, others
        assertEquals(16, report.rows.size, "Project1 report should contain 8 rows per team (2 teams * 8 = 16)")
    }

    @Test
    fun testTeamAnalyzerShouldReportCorrectRowNames() {
        val report = analyzer.process()
        val teamNames = report.rows.map { it.name }.toSet()
        val expectedTeamNames = setOf("team1", "team2")
        assertEquals(expectedTeamNames, teamNames, "Should report the correct team names")

        // Check that we have the correct tags for each team
        val team1Tags = report.rows.filter { it.name == "team1" }.mapNotNull { row ->
            row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value
        }.toSet()
        val expectedTags = setOf("total", "android-java-libraries", "native-libraries", "codebase-kotlin-java", "codebase-resources", "codebase-assets", "codebase-native", "others")
        assertEquals(expectedTags, team1Tags, "team1 should have all breakdown tags")
    }

    @Test
    fun testTeamAnalyzerShouldReportCorrectTeam1Size() {
        val report = analyzer.process()
        val team1TotalRow = report.rows.find { row ->
            row.name == "team1" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team1TotalRow, "Project1 report should contain team1 total size row")
        assertEquals(125L, team1TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value)
    }

    @Test
    fun testTeamAnalyzerShouldReportCorrectTeam2Size() {
        val report = analyzer.process()
        val team2TotalRow = report.rows.find { row ->
            row.name == "team2" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team2TotalRow, "Project1 report should contain team2 total size row")
        assertEquals(147L, team2TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value)
    }

    @Test
    fun testTeamAnalyzerShouldReportCorrectContributorFields() {
        val report = analyzer.process()
        // Check that each row has proper contributor field showing metric type and owner field showing team name
        val team1TotalRow = report.rows.find { row ->
            row.name == "team1" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team1TotalRow, "Should have team1 total row")
        val contributorField = team1TotalRow.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }
        assertNotNull(contributorField, "Each row should have a contributor field")
        assertEquals("total", contributorField.value, "Contributor should show metric type")

        val ownerField = team1TotalRow.fields.find { it.name == FIELD_KEY_OWNER }
        assertNotNull(ownerField, "Each row should have an owner field")
        assertEquals("team1", ownerField.value, "Owner should match the team name")
    }

    @Test
    fun testTeamAnalyzerWithLibraryContributions() {
        // Create test data with library ownership
        val projectDataWithLibs = ProjectDataWithLibraryOwnership()
        val analyzerWithLibs = TeamAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = projectDataWithLibs.fakeDataPasser,
            teamMapping = projectDataWithLibs.teamMapping
        )

        val report = analyzerWithLibs.process()

        // Verify correct number of rows: 2 teams * 8 breakdown rows each = 16 total
        assertEquals(16, report.rows.size)

        // Get actual sizes for validation from the total rows
        val team1TotalRow = report.rows.find { row ->
            row.name == "team1" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team1TotalRow, "Should contain team1 total row")
        val team1Size = team1TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value as Long

        val team2TotalRow = report.rows.find { row ->
            row.name == "team2" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team2TotalRow, "Should contain team2 total row")
        val team2Size = team2TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value as Long

        // Get sizes from Project1Data (which now includes some library ownership)
        val originalReport = analyzer.process()
        val originalTeam1Size = originalReport.rows.find { row ->
            row.name == "team1" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value as Long
        val originalTeam2Size = originalReport.rows.find { row ->
            row.name == "team2" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value as Long

        // ProjectDataWithLibraryOwnership gives team1 libAar1 (15 bytes) and team2 libAar2 (40 bytes)
        // but doesn't assign libJar1 (7 bytes) to anyone (unlike Project1Data which assigns it to team1)
        // So the differences should be:
        // team1: should be 7 bytes less than Project1Data (loses libJar1)
        // team2: should be same as Project1Data (gets libAar2 in both)

        assertEquals(118L, team1Size, "team1 should have modules (103) + libAar1 (15) = 118")
        assertEquals(147L, team2Size, "team2 should have modules (107) + libAar2 (40) = 147")

        // Verify the changes are as expected
        assertEquals(-7L, team1Size - originalTeam1Size, "team1 should lose libJar1 (7 bytes)")
        assertEquals(0L, team2Size - originalTeam2Size, "team2 should have same library contribution")
    }

    @Test
    fun testTeamAnalyzerWithLibraryOnlyTeams() {
        // Test scenario where some teams only own libraries (no modules)
        val projectDataLibraryOnly = ProjectDataWithLibraryOnlyTeams()
        val analyzerLibraryOnly = TeamAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = projectDataLibraryOnly.fakeDataPasser,
            teamMapping = projectDataLibraryOnly.teamMapping
        )

        val report = analyzerLibraryOnly.process()

        // Should have 3 teams * 8 breakdown rows each = 24 total rows
        assertEquals(24, report.rows.size, "Should include all teams including library-only teams")

        val totalRowNames = report.rows.filter { row ->
            row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }.map { it.name }.toSet()
        val expectedTotalRows = setOf("team1", "team2", "libraryTeam")
        assertEquals(expectedTotalRows, totalRowNames, "Should include total rows for all teams including library-only team")

        // Verify library-only team has correct size
        val libraryOnlyTotalRow = report.rows.find { row ->
            row.name == "libraryTeam" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(libraryOnlyTotalRow, "Library-only team total row should be included")

        // libraryTeam owns libJar1 (7 bytes)
        val libraryTeamSize = libraryOnlyTotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value as Long
        assertEquals(7L, libraryTeamSize, "Library-only team should have library size (7 bytes)")
    }

    @Test
    fun testTeamAnalyzerShouldHandleModuleAarNotBelongToBuildFolder() {
        val project2Data = Project2Data()
        val project2Analyzer = TeamAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = project2Data.fakeDataPasser,
            teamMapping = project2Data.teamMapping
        )

        val report = project2Analyzer.process()

        // Verify basic structure (same as Project1 but with different paths)
        assertEquals(16, report.rows.size, "Should have 8 breakdown rows per team * 2 teams = 16 total")

        // Verify team total sizes are the same as Project1 (since data should be equivalent including libraries)
        val team1TotalRow = report.rows.find { row ->
            row.name == "team1" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team1TotalRow, "Should have team1 total row")
        assertEquals(125L, team1TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value)

        val team2TotalRow = report.rows.find { row ->
            row.name == "team2" && row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }?.value == "total"
        }
        assertNotNull(team2TotalRow, "Should have team2 total row")
        assertEquals(147L, team2TotalRow.fields.find { it.name == FIELD_KEY_SIZE }?.value)
    }
}

/**
 * Test data with library ownership mapping for comprehensive library contribution testing
 */
class ProjectDataWithLibraryOwnership {
    private val project1Data = Project1Data()

    val fakeDataPasser = project1Data.fakeDataPasser

    val teamMapping = object : TeamMapping {
        override fun getModuleOwner(moduleName: String): String? {
            return when (moduleName) {
                "moduleAar1", "moduleJar1" -> "team1"
                "moduleAar2", "moduleJar2" -> "team2"
                else -> null
            }
        }

        override fun getLibraryOwner(libraryCoordinate: String): String? {
            return when (libraryCoordinate) {
                "libAar1" -> "team1"     // Assign libAar1 (15 bytes) to team1
                "libAar2" -> "team2"     // Assign libAar2 (40 bytes) to team2
                "libJar1" -> null        // libJar1 (7 bytes) remains unassigned
                else -> null
            }
        }

        override fun getAllTeams(): Set<String> = setOf("team1", "team2")
        override fun getModuleTeams(): Set<String> = setOf("team1", "team2")
        override fun getLibraryTeams(): Set<String> = setOf("team1", "team2")
    }
}

/**
 * Test data with library-only teams to verify teams that own no modules but only libraries
 */
class ProjectDataWithLibraryOnlyTeams {
    private val project1Data = Project1Data()

    val fakeDataPasser = project1Data.fakeDataPasser

    val teamMapping = object : TeamMapping {
        override fun getModuleOwner(moduleName: String): String? {
            return when (moduleName) {
                "moduleAar1", "moduleJar1" -> "team1"
                "moduleAar2", "moduleJar2" -> "team2"
                // No modules assigned to "libraryTeam"
                else -> null
            }
        }

        override fun getLibraryOwner(libraryCoordinate: String): String? {
            return when (libraryCoordinate) {
                "libAar1" -> "team1"         // team1: modules + library
                "libAar2" -> null            // libAar2 unassigned
                "libJar" -> "libraryTeam"    // libraryTeam: library ONLY (no modules) - FIXED!
                else -> null
            }
        }

        override fun getAllTeams(): Set<String> = setOf("team1", "team2", "libraryTeam")
        override fun getModuleTeams(): Set<String> = setOf("team1", "team2")
        override fun getLibraryTeams(): Set<String> = setOf("team1", "libraryTeam")
    }
}

class Project2Data : Project1Data() {
    override val moduleAar1: AarFileInfo
        get() = super.moduleAar1.copy(path = "aar/moduleAar1.aar")
    override val moduleAar2: AarFileInfo
        get() = super.moduleAar2.copy(path = "aar/moduleAar2.aar")
    override val moduleJar1: JarFileInfo
        get() = super.moduleJar1.copy(path = "jar/moduleJar1.jar")
    override val moduleJar2: JarFileInfo
        get() = super.moduleJar2.copy(path = "jar/moduleJar2.jar")
}