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

/**
 * This is more likely an integration test, not just unit test
 */
class ApkAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val apkAnalyzer = ApkAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser
    )

    @Test
    fun testApkAnalyzerWithProject1Data() {
        val report = apkAnalyzer.process().sort()
        assertEquals(expectedProject1Report.sort(), report)
    }

    @Test
    fun testApkAnalyzerShouldReportProperApkSize() {
        val report = apkAnalyzer.process()
        val apkRow = report.rows.find { it.name == "Apk" }
        assertEquals(expectApkRow, apkRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperCodebaseKotlinJavaSize() {
        val report = apkAnalyzer.process()
        val codebaseKotlinJavaRow = report.rows.find { it.name == "codebase-kotlin-java" }
        assertEquals(expectCodebaseKotlinJavaRow, codebaseKotlinJavaRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperCodebaseResourcesSize() {
        val report = apkAnalyzer.process()
        val codebaseResourcesRow = report.rows.find { it.name == "codebase-resources" }
        assertEquals(expectCodebaseResourcesRow, codebaseResourcesRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperCodebaseAssetsSize() {
        val report = apkAnalyzer.process()
        val codebaseAssetsRow = report.rows.find { it.name == "codebase-assets" }
        assertEquals(expectCodebaseAssetsRow, codebaseAssetsRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperCodebaseNativeSize() {
        val report = apkAnalyzer.process()
        val codebaseNativeRow = report.rows.find { it.name == "codebase-native" }
        assertEquals(expectCodebaseNativeRow, codebaseNativeRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperOthersSize() {
        val report = apkAnalyzer.process()
        val othersRow = report.rows.find { it.name == "others" }
        assertEquals(expectOthersRow, othersRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperAndroidJavaLibrariesSize() {
        val report = apkAnalyzer.process()
        val androidJavaLibrariesRow = report.rows.find { it.name == "android-java-libraries" }
        assertEquals(expectAndroidJavaLibrariesRow, androidJavaLibrariesRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperNativeLibrariesSize() {
        val report = apkAnalyzer.process()
        val nativeLibrariesRow = report.rows.find { it.name == "native-libraries" }
        assertEquals(expectNativeLibrariesRow, nativeLibrariesRow)
    }

    private val expectApkRow = Row(
        name = "Apk",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "apk"),
            DefaultField(name = FIELD_KEY_SIZE, value = 292L)
        )
    )

    private val expectCodebaseKotlinJavaRow = Row(
        name = "codebase-kotlin-java",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-kotlin-java"),
            DefaultField(name = FIELD_KEY_SIZE, value = 40L)
        )
    )

    private val expectCodebaseResourcesRow = Row(
        name = "codebase-resources",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-resources"),
            DefaultField(name = FIELD_KEY_SIZE, value = 40L)
        )
    )

    private val expectCodebaseAssetsRow = Row(
        name = "codebase-assets",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-assets"),
            DefaultField(name = FIELD_KEY_SIZE, value = 50L)
        )
    )

    private val expectCodebaseNativeRow = Row(
        name = "codebase-native",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-native"),
            DefaultField(name = FIELD_KEY_SIZE, value = 80L)
        )
    )

    private val expectOthersRow = Row(
        name = "others",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "others"),
            DefaultField(name = FIELD_KEY_SIZE, value = 20L)
        )
    )

    private val expectAndroidJavaLibrariesRow = Row(
        name = "android-java-libraries",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "android-java-libraries"),
            DefaultField(name = FIELD_KEY_SIZE, value = 52L)
        )
    )

    private val expectNativeLibrariesRow = Row(
        name = "native-libraries",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "native-libraries"),
            DefaultField(name = FIELD_KEY_SIZE, value = 10L)
        )
    )

    private val expectedProject1Report = Report(
        id = "apk",
        name = "apk",
        rows = listOf(
            expectApkRow,
            expectCodebaseKotlinJavaRow,
            expectCodebaseResourcesRow,
            expectCodebaseAssetsRow,
            expectCodebaseNativeRow,
            expectOthersRow,
            expectAndroidJavaLibrariesRow,
            expectNativeLibrariesRow
        )
    )
}

internal fun Report.sort(): Report {
    return this.copy(
        rows = this.rows.map { row ->
            row.copy(
                fields = row.fields.sortedBy { it.name }
            )
        }.sortedBy { it.name }
    )
}