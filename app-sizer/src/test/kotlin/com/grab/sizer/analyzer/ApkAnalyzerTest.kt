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

    private val expectedProject1Report = Report(
        id = "apk",
        name = "apk",
        rows = listOf(
            Row(
                name = "Apk",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "apk"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 292L)
                )
            ),
            Row(
                name = "codebase-kotlin-java",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-kotlin-java"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 40L)
                )
            ),
            Row(
                name = "codebase-resources",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-resources"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 40L)
                )
            ),
            Row(
                name = "codebase-assets",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-assets"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 50L)
                )
            ),
            Row(
                name = "codebase-native",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "codebase-native"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 80L)
                )
            ),
            Row(
                name = "others",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "others"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "android-java-libraries",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "android-java-libraries"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 52L)
                )
            ),
            Row(
                name = "native-libraries",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "native-libraries"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 10L)
                )
            )
        )
    )
}