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

class BasicApkAnalyzerTest {
    private val project1Data = Project1Data()
    private val apkAnalyzer = BasicApkAnalyzer(dataParser = project1Data.fakeDataPasser)

    @Test
    fun testBasicApkAnalyzerWithProject1Data() {
        val report = apkAnalyzer.process().sort()
        assertEquals(expectedProject1Report.sort(), report)
    }

    @Test
    fun testBasicApkAnalyzerShouldReportProperApkSize() {
        val report = apkAnalyzer.process()
        val apkRow = report.rows.find { it.name == "apk" }
        assertEquals(expectApkRow, apkRow)
    }

    @Test
    fun testBasicApkAnalyzerShouldReportProperResourceSize() {
        val report = apkAnalyzer.process()
        val resourceRow = report.rows.find { it.name == "resource" }
        assertEquals(expectResourceRow, resourceRow)
    }

    @Test
    fun testBasicApkAnalyzerShouldReportProperNativeLibSize() {
        val report = apkAnalyzer.process()
        val nativeLibRow = report.rows.find { it.name == "native_lib" }
        assertEquals(expectNativeLibRow, nativeLibRow)
    }

    @Test
    fun testBasicApkAnalyzerShouldReportProperAssetSize() {
        val report = apkAnalyzer.process()
        val assetRow = report.rows.find { it.name == "asset" }
        assertEquals(expectAssetRow, assetRow)
    }

    @Test
    fun testBasicApkAnalyzerShouldReportProperOtherSize() {
        val report = apkAnalyzer.process()
        val otherRow = report.rows.find { it.name == "other" }
        assertEquals(expectOtherRow, otherRow)
    }

    @Test
    fun testBasicApkAnalyzerShouldReportProperCodeSize() {
        val report = apkAnalyzer.process()
        val codeRow = report.rows.find { it.name == "code" }
        assertEquals(expectCodeRow, codeRow)
    }

    private val expectApkRow = Row(
        name = "apk",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "apk"),
            DefaultField(name = FIELD_KEY_SIZE, value = 292L)
        )
    )

    private val expectResourceRow = Row(
        name = "resource",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "resource"),
            DefaultField(name = FIELD_KEY_SIZE, value = 70L)
        )
    )

    private val expectNativeLibRow = Row(
        name = "native_lib",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "native_lib"),
            DefaultField(name = FIELD_KEY_SIZE, value = 90L)
        )
    )

    private val expectAssetRow = Row(
        name = "asset",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "asset"),
            DefaultField(name = FIELD_KEY_SIZE, value = 60L)
        )
    )

    private val expectOtherRow = Row(
        name = "other",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "other"),
            DefaultField(name = FIELD_KEY_SIZE, value = 20L)
        )
    )

    private val expectCodeRow = Row(
        name = "code",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "code"),
            DefaultField(name = FIELD_KEY_SIZE, value = 52L)
        )
    )

    private val expectedProject1Report = Report(
        id = "apk_basic",
        name = "apk_basic",
        rows = listOf(
            expectApkRow,
            expectResourceRow,
            expectNativeLibRow,
            expectAssetRow,
            expectOtherRow,
            expectCodeRow
        )
    )
}