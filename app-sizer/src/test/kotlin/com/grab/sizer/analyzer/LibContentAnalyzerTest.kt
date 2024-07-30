package com.grab.sizer.analyzer

import com.google.gson.Gson
import com.grab.sizer.report.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LibContentAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = LibContentAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        libName = project1Data.libAar1.name
    )

    @Test
    fun testLibContentAnalyzerWithProject1Data() {
        val report = analyzer.process()
        println(Gson().toJson(report))
        assertEquals(expectedProject1Report, report)
    }

    @Test
    fun testLibContentAnalyzerShouldReportCorrectNumberOfItems() {
        val report = analyzer.process()
        assertEquals("Should report 2 items in the library", 2, report.rows.size.toLong())
    }

    @Test
    fun testLibContentAnalyzerShouldReportCorrectItemNames() {
        val report = analyzer.process()
        val itemNames = report.rows.map { it.name }.toSet()
        val expectedNames = setOf("asset_resource_1.xml", "com.grab.test.HelloWorld")
        assertEquals("Should report the correct item names", expectedNames, itemNames)
    }

    @Test
    fun testLibContentAnalyzerShouldReportCorrectItemTypes() {
        val report = analyzer.process()
        val assetItem = report.rows.find { it.name == "asset_resource_1.xml" }
        val classItem = report.rows.find { it.name == "com.grab.test.HelloWorld" }

        assertEquals(
            "asset_resource_1.xml should be tagged as Asset",
            "Asset",
            assetItem?.fields?.find { it.name == FIELD_KEY_TAG }?.value
        )
        assertEquals(
            "com.grab.test.HelloWorld should be tagged as Class",
            "Class",
            classItem?.fields?.find { it.name == FIELD_KEY_TAG }?.value
        )
    }

    @Test
    fun testLibContentAnalyzerShouldReportCorrectItemSizes() {
        val report = analyzer.process()
        val assetItem = report.rows.find { it.name == "asset_resource_1.xml" }
        val classItem = report.rows.find { it.name == "com.grab.test.HelloWorld" }

        assertEquals(
            "asset_resource_1.xml should have size 10",
            10L,
            assetItem?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
        assertEquals(
            "com.grab.test.HelloWorld should have size 5",
            5L,
            classItem?.fields?.find { it.name == FIELD_KEY_SIZE }?.value
        )
    }


    private val expectedProject1Report = Report(
        id = "library_content",
        name = "library_content",
        rows = listOf(
            Row(
                name = "asset_resource_1.xml",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "asset_resource_1.xml"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "Asset"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 10L)
                )
            ),
            Row(
                name = "com.grab.test.HelloWorld",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "com.grab.test.HelloWorld"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "Class"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 5L)
                )
            )
        )
    )
}