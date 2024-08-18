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
import org.junit.Test
import kotlin.test.assertEquals

class LibrariesAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = LibrariesAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser
    )

    @Test
    fun testApkAnalyzerWithProject1Data() {
        val report = analyzer.process().sort()
        assertEquals(expectedProject1Report.sort(), report)
    }

    @Test
    fun testLibrariesAnalyzerShouldReportCorrectNumberOfLibraries() {
        val report = analyzer.process()
        assertEquals(3, report.rows.size, "Should report 3 libraries")
    }

    @Test
    fun testLibrariesAnalyzerShouldReportCorrectLibraryNames() {
        val report = analyzer.process()
        val libraryNames = report.rows.map { it.name }.toSet()
        val expectedNames = setOf("libJar", "libAar1", "libAar2")
        assertEquals(expectedNames, libraryNames, "Should report the correct library names")
    }

    @Test
    fun testLibrariesAnalyzerShouldReportCorrectLibrarySizes() {
        val report = analyzer.process()
        val libJar = report.rows.find { it.name == "libJar" }
        val libAar1 = report.rows.find { it.name == "libAar1" }
        val libAar2 = report.rows.find { it.name == "libAar2" }

        assertEquals(7L, libJar?.fields?.find { it.name == FIELD_KEY_SIZE }?.value, "libJar should have size 7")
        assertEquals(15L, libAar1?.fields?.find { it.name == FIELD_KEY_SIZE }?.value, "libAar1 should have size 15")
        assertEquals(40L, libAar2?.fields?.find { it.name == FIELD_KEY_SIZE }?.value, "libAar2 should have size 40")
    }

    private val expectedProject1Report = Report(
        id = "library",
        name = "library",
        rows = listOf(
            Row(
                name = "libJar",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "libJar"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 7L)
                )
            ),
            Row(
                name = "libAar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "libAar1"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 15L)
                )
            ),
            Row(
                name = "libAar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "libAar2"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 40L)
                )
            )
        )
    )
}