package com.grab.sizer.analyzer

import com.grab.sizer.report.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuleAnalyzerTest {
    private val mapperComponent = MapperComponent()
    private val project1Data = Project1Data()
    private val analyzer = ModuleAnalyzer(
        apkComponentProcessor = mapperComponent.apkComponentProcessor,
        dataParser = project1Data.fakeDataPasser,
        teamMapping = project1Data.teamMapping
    )

    @Test
    fun testModuleAnalyzerWithProject1Data() {
        val report = analyzer.process()
        assertEquals(expectedProject1Report, report)
    }

    @Test
    fun testModuleAnalyzerShouldHandleModuleAarNotBelongToBuildFolder() {
        val project2Data = Project2Data()
        val analyzer = ModuleAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = project2Data.fakeDataPasser,
            teamMapping = project2Data.teamMapping
        )

        val report = analyzer.process()
        assertEquals(expectedProject2Report, report)
    }

    private val expectedProject1Report = Report(
        id = "module",
        name = "module",
        rows = listOf(
            Row(
                name = "moduleJar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 13L)
                )
            ),
            Row(
                name = "moduleJar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 18L)
                )
            ),
            Row(
                name = "app",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "app"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "moduleAar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 89L)
                )
            ),
            Row(
                name = "moduleAar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 90L)
                )
            )
        )
    )

    private val expectedProject2Report = Report(
        id = "module",
        name = "module",
        rows = listOf(
            Row(
                name = "moduleJar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 13L)
                )
            ),
            Row(
                name = "moduleJar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleJar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 18L)
                )
            ),
            Row(
                name = "app",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "app"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 20L)
                )
            ),
            Row(
                name = "moduleAar2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar2"),
                    TagField(name = FIELD_KEY_OWNER, value = "team2"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 89L)
                )
            ),
            Row(
                name = "moduleAar1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "moduleAar1"),
                    TagField(name = FIELD_KEY_OWNER, value = "team1"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 90L)
                )
            )
        )
    )
}