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

class CodebaseAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = CodebaseAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        teamMapping = project1Data.teamMapping
    )

    @Test
    fun testCodebaseAnalyzerWithProject1Data() {
        val report = analyzer.process().sort()
        assertEquals(expectedProject1Report.sort(), report)
    }

    @Test
    fun testCodebaseAnalyzerShouldReportCorrectNumberOfTeams() {
        val report = analyzer.process()
        assertEquals(2, report.rows.size, "Project1 report should contain exactly 2 teams")
    }

    @Test
    fun testCodebaseAnalyzerShouldReportCorrectTeamNames() {
        val report = analyzer.process()
        val teamNames = report.rows.map { it.name }.toSet()
        val expectedTeamNames = setOf("team1", "team2")
        assertEquals(expectedTeamNames, teamNames, "Should report the correct team names")
    }

    @Test
    fun testCodebaseAnalyzerShouldReportCorrectTeam1Size() {
        val report = analyzer.process()
        val team1Row = report.rows.find { it.name == "team1" }
        assertNotNull(team1Row, "Project1 report should contain team1")
        assertEquals(103L, team1Row.fields.find { it.name == FIELD_KEY_SIZE }?.value)
    }

    @Test
    fun testCodebaseAnalyzerShouldReportCorrectTeam2Size() {
        val report = analyzer.process()
        val team2Row = report.rows.find { it.name == "team2" }
        assertNotNull(team2Row, "Project1 report should contain team2")
        assertEquals(107L, team2Row.fields.find { it.name == FIELD_KEY_SIZE }?.value)
    }

    @Test
    fun testCodebaseAnalyzerShouldReportCorrectContributorFields() {
        val report = analyzer.process()
        report.rows.forEach { row ->
            val contributorField = row.fields.find { it.name == FIELD_KEY_CONTRIBUTOR }
            assertNotNull(contributorField, "Each row should have a contributor field")
            assertEquals(row.name, contributorField.value, "Contributor should match the team name")
        }
    }

    @Test
    fun testCodebaseAnalyzerShouldHandleModuleAarNotBelongToBuildFolder() {
        val project2Data = Project2Data()
        val project2Analyzer = CodebaseAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = project2Data.fakeDataPasser,
            teamMapping = project2Data.teamMapping
        )

        val report = project2Analyzer.process()
        assertEquals(expectedProject2Report, report)
    }

    private val expectedProject1Report = Report(
        id = "team",
        name = "team",
        rows = listOf(
            Row(
                name = "team2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 107L)
                )
            ),
            Row(
                name = "team1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 103L)
                )
            )
        )
    )

    private val expectedProject2Report = Report(
        id = "team",
        name = "team",
        rows = listOf(
            Row(
                name = "team2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 107L)
                )
            ),
            Row(
                name = "team1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 103L)
                )
            )
        )
    )
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