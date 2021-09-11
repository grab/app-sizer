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
import org.junit.Assert.assertTrue
import org.junit.Test

private const val THRESHOLD = 20L

class LargeFileAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = LargeFileAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        teamMapping = project1Data.teamMapping,
        largeFileThreshold = THRESHOLD
    )

    @Test
    fun testLargeFileAnalyzerWithProject1Data() {
        val report = analyzer.process().sort()
        assertEquals(
            expectedProject1Report.sort(),
            report
        )
    }

    @Test
    fun testLargeFileAnalyzerShouldReportCorrectNumberOfLargeFiles() {
        val report = analyzer.process()
        assertEquals("Should report 4 large files", 4, report.rows.size.toLong())
    }

    @Test
    fun testLargeFileAnalyzerShouldReportCorrectFileNames() {
        val report = analyzer.process()
        val fileNames = report.rows.map { it.name }.toSet()
        val expectedFileNames =
            setOf("test_font.xml", "asset_resource_2.xml", "test_animator.xml", "asset_resource_3.xml")
        assertEquals("Should report the correct file names", expectedFileNames, fileNames)
    }

    @Test
    fun testLargeFileAnalyzerShouldReportCorrectTeamOwnership() {
        val report = analyzer.process()
        val team1Files =
            report.rows.filter { it.fields.find { field -> field.name == FIELD_KEY_OWNER }?.value == "team1" }
        val team2Files =
            report.rows.filter { it.fields.find { field -> field.name == FIELD_KEY_OWNER }?.value == "team2" }

        assertEquals("Team1 should own 2 large files", 2, team1Files.size.toLong())
        assertEquals("Team2 should own 2 large files", 2, team2Files.size.toLong())
    }

    @Test
    fun testLargeFileAnalyzerShouldReportCorrectModuleTags() {
        val report = analyzer.process()
        val moduleAar1Files =
            report.rows.filter { it.fields.find { field -> field.name == FIELD_KEY_TAG }?.value == "moduleAar1" }
        val moduleAar2Files =
            report.rows.filter { it.fields.find { field -> field.name == FIELD_KEY_TAG }?.value == "moduleAar2" }

        assertEquals("ModuleAar1 should contain 2 large files", 2, moduleAar1Files.size.toLong())
        assertEquals("ModuleAar2 should contain 2 large files", 2, moduleAar2Files.size.toLong())
    }

    @Test
    fun testLargeFileAnalyzerShouldReportFileSizeLargerOrEqualToThreshold() {
        val report = analyzer.process()
        report.rows.forEach { row ->
            val size = row.fields.find { it.name == FIELD_KEY_SIZE }?.value as? Long
            assertTrue("All reported files should be at least 20 bytes", size != null && size >= THRESHOLD)
        }
    }


    private val expectedProject1Report = Report(
        id = "large_file",
        name = "large_file",
        rows = listOf(
            Row(
                name = "test_font.xml",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "test_font.xml"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    TagField(name = FIELD_KEY_TAG, value = "moduleAar1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "asset_resource_2.xml",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "asset_resource_2.xml"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    TagField(name = FIELD_KEY_TAG, value = "moduleAar1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "test_animator.xml",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "test_animator.xml"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    TagField(name = FIELD_KEY_TAG, value = "moduleAar2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "asset_resource_3.xml",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "asset_resource_3.xml"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    TagField(name = FIELD_KEY_TAG, value = "moduleAar2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 30L)
                )
            )
        )
    )
}