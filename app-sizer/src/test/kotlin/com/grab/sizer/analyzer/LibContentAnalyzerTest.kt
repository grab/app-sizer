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

    val expectedProject1Report = Report(
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