package com.grab.sizer.analyzer

import com.grab.sizer.report.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BasicApkAnalyzerTest {
    private val project1Data = Project1Data()
    private val apkAnalyzer = BasicApkAnalyzer(dataParser = project1Data.fakeDataPasser)

    @Test
    fun testBasicApkAnalyzerWithProject1Data() {
        val report = apkAnalyzer.process()
        assertEquals(expectedProject1Report, report)
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
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 292L)
        )
    )

    private val expectResourceRow = Row(
        name = "resource",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "resource"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 70L)
        )
    )

    private val expectNativeLibRow = Row(
        name = "native_lib",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "native_lib"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 90L)
        )
    )

    private val expectAssetRow = Row(
        name = "asset",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "asset"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 60L)
        )
    )

    private val expectOtherRow = Row(
        name = "other",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "other"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 20L)
        )
    )

    private val expectCodeRow = Row(
        name = "code",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "code"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
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