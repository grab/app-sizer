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
        val report = apkAnalyzer.process()
        assertEquals(expectedProject1Report, report)
    }

    @Test
    fun testApkAnalyzerShouldReportProperApkSize(){
        val report = apkAnalyzer.process()
        val apkRow = report.rows.find { it.name == "Apk" }
        assertEquals(expectApkRow, apkRow)
    }

    @Test
    fun testApkAnalyzerShouldReportProperCodebaseKotlinJavaSize(){
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
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 40L)
        )
    )

    private val expectCodebaseResourcesRow = Row(
        name = "codebase-resources",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-resources"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 40L)
        )
    )

    private val expectCodebaseAssetsRow = Row(
        name = "codebase-assets",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-assets"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 50L)
        )
    )

    private val expectCodebaseNativeRow = Row(
        name = "codebase-native",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-native"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 80L)
        )
    )

    private val expectOthersRow = Row(
        name = "others",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "others"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 20L)
        )
    )

    private val expectAndroidJavaLibrariesRow = Row(
        name = "android-java-libraries",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "android-java-libraries"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
            DefaultField(name = FIELD_KEY_SIZE, value = 52L)
        )
    )

    private val expectNativeLibrariesRow = Row(
        name = "native-libraries",
        fields = listOf(
            TagField(name = FIELD_KEY_CONTRIBUTOR, value = "native-libraries"),
            TagField(name = FIELD_KEY_OWNER, value = "NA"),
            TagField(name = FIELD_KEY_TAG, value = "NA"),
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