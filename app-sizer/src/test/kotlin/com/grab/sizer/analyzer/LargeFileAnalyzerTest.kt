package com.grab.sizer.analyzer

import com.google.gson.Gson
import com.grab.sizer.report.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LargeFileAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = LargeFileAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        teamMapping = project1Data.teamMapping,
        largeFileThreshold = 20
    )

    @Test
    fun testLargeFileAnalyzerWithProject1Data() {
        val report = analyzer.process()
        assertEquals(expectedProject1Report, report)
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