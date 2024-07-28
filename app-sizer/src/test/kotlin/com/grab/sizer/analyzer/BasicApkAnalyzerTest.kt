package com.grab.sizer.analyzer

import com.google.gson.Gson
import com.grab.sizer.report.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BasicApkAnalyzerTest {
    private val project1Data = Project1Data()
    private val apkAnalyzer = BasicApkAnalyzer(dataParser = project1Data.fakeDataPasser)

    @Test
    fun testBasicApkAnalyzerWithProject1Data() {
        val report = apkAnalyzer.process()
        println(Gson().toJson(report))
        assertEquals(expectedProject1Report, report)
    }

    private val expectedProject1Report = Report(
        id = "apk_basic",
        name = "apk_basic",
        rows = listOf(
            Row(
                name = "apk",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "apk"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 292L)
                )
            ),
            Row(
                name = "resource",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "resource"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 70L)
                )
            ),
            Row(
                name = "native_lib",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "native_lib"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 90L)
                )
            ),
            Row(
                name = "asset",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "asset"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 60L)
                )
            ),
            Row(
                name = "other",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "other"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "code",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "code"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 52L)
                )
            )
        )
    )
}