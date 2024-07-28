package com.grab.sizer.analyzer

import com.google.gson.Gson
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