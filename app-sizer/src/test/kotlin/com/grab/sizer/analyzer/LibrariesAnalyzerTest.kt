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
        val report = analyzer.process()
        assertEquals(expectedProject1Report, report)
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
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 7L)
                )
            ),
            Row(
                name = "libAar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "libAar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 15L)
                )
            ),
            Row(
                name = "libAar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "libAar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 40L)
                )
            )
        )
    )
}