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

import com.grab.sizer.report.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuleAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = ModuleAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        teamMapping = project1Data.teamMapping
    )

    @Test
    fun testModuleAnalyzerWithProject1Data() {
        val report = analyzer.process().sort()
        assertEquals(expectedProject1Report.sort(), report)
    }


    @Test
    fun testModuleAnalyzerShouldReportCorrectNumberOfModules() {
        val report = analyzer.process()
        assertEquals("Should report 5 modules", 5, report.rows.size)
    }

    @Test
    fun testModuleAnalyzerShouldReportCorrectModuleNames() {
        val report = analyzer.process()
        val moduleNames = report.rows.map { it.name }.toSet()
        val expectedNames = setOf("moduleJar1", "moduleJar2", "app", "moduleAar2", "moduleAar1")
        assertEquals("Should report the correct module names", expectedNames, moduleNames)
    }

    @Test
    fun testModuleAnalyzerShouldReportCorrectModuleSizes() {
        val report = analyzer.process()
        assertEquals(
            "moduleJar1 should have size 13",
            13L,
            report.rows.find { it.name == "moduleJar1" }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
        assertEquals(
            "moduleJar2 should have size 18",
            18L,
            report.rows.find { it.name == "moduleJar2" }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
        assertEquals(
            "app should have size 20",
            20L,
            report.rows.find { it.name == "app" }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
        assertEquals(
            "moduleAar2 should have size 89",
            89L,
            report.rows.find { it.name == "moduleAar2" }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
        assertEquals(
            "moduleAar1 should have size 90",
            90L,
            report.rows.find { it.name == "moduleAar1" }?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
    }

    @Test
    fun testModuleAnalyzerShouldReportCorrectTeamOwnership() {
        val report = analyzer.process()
        assertEquals(
            "moduleJar1 should be owned by team1",
            "team1",
            report.rows.find { it.name == "moduleJar1" }?.fields?.find { it.name == FIELD_KEY_OWNER }?.value
        )
        assertEquals(
            "moduleJar2 should be owned by team2",
            "team2",
            report.rows.find { it.name == "moduleJar2" }?.fields?.find { it.name == FIELD_KEY_OWNER }?.value
        )
        assertEquals(
            "app should have no team ownership",
            "NA",
            report.rows.find { it.name == "app" }?.fields?.find { it.name == FIELD_KEY_OWNER }?.value
        )
        assertEquals(
            "moduleAar2 should be owned by team2",
            "team2",
            report.rows.find { it.name == "moduleAar2" }?.fields?.find { it.name == FIELD_KEY_OWNER }?.value
        )
        assertEquals(
            "moduleAar1 should be owned by team1",
            "team1",
            report.rows.find { it.name == "moduleAar1" }?.fields?.find { it.name == FIELD_KEY_OWNER }?.value
        )
    }


    @Test
    fun testModuleAnalyzerShouldHandleModuleAarNotBelongToBuildFolder() {
        val project2Data = Project2Data()
        val analyzer = ModuleAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = project2Data.fakeDataPasser,
            teamMapping = project2Data.teamMapping
        )

        val report = analyzer.process().sort()
        assertEquals(expectedProject2Report.sort(), report)
    }

    private val expectedProject1Report = Report(
        id = "module",
        name = "module",
        rows = listOf(
            Row(
                name = "moduleJar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 13L)
                )
            ),
            Row(
                name = "moduleJar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 18L)
                )
            ),
            Row(
                name = "app",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "app"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "moduleAar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 89L)
                )
            ),
            Row(
                name = "moduleAar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 90L)
                )
            )
        )
    )

    private val expectedProject2Report = Report(
        id = "module",
        name = "module",
        rows = listOf(
            Row(
                name = "moduleJar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 13L)
                )
            ),
            Row(
                name = "moduleJar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 18L)
                )
            ),
            Row(
                name = "app",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "app"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "moduleAar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 89L)
                )
            ),
            Row(
                name = "moduleAar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 90L)
                )
            )
        )
    )
}