package com.grab.sizer.analyzer

import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.JarFileInfo
import com.grab.sizer.report.*
import org.junit.Test
import kotlin.test.assertEquals

class CodebaseAnalyzerTest {
    private val mapperComponent = MapperComponent()

    @Test
    fun testCodebaseAnalyzerWithProject1Data() {
        val project1Data = Project1Data()
        val apkAnalyzer = CodebaseAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = project1Data.fakeDataPasser,
            teamMapping = project1Data.teamMapping
        )

        val report = apkAnalyzer.process()
        assertEquals(expectedProject1Report, report)
    }

    @Test
    fun testCodebaseAnalyzerShouldHandleModuleAarNotBelongToBuildFolder() {
        val project2Data = Project2Data()
        val apkAnalyzer = CodebaseAnalyzer(
            apkComponentProcessor = mapperComponent.apkComponentProcessor,
            dataParser = project2Data.fakeDataPasser,
            teamMapping = project2Data.teamMapping
        )

        val report = apkAnalyzer.process()
        assertEquals(expectedProject2Report, report)
    }

    private val expectedProject1Report = Report(
        id = "team",
        name = "team",
        rows = listOf(
            Row(
                name = "team2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team2"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 107L)
                )
            ),
            Row(
                name = "team1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team1"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 103L)
                )
            )
        )
    )

    private val expectedProject2Report = Report(
        id = "team",
        name = "team",
        rows = listOf(
            Row(
                name = "team2",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team2"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 107L)
                )
            ),
            Row(
                name = "team1",
                fields = listOf(
                    TagField(name = FIELD_KEY_CONTRIBUTOR, value = "team1"),
                    TagField(name = FIELD_KEY_OWNER, value = "NA"),
                    TagField(name = FIELD_KEY_TAG, value = "NA"),
                    DefaultField(name = FIELD_KEY_SIZE, value = 103L)
                )
            )
        )
    )

}


class Project2Data : Project1Data() {
    override val moduleAar1: AarFileInfo
        get() = super.moduleAar1.copy(path = "aar/moduleAar1.aar")
    override val moduleAar2: AarFileInfo
        get() = super.moduleAar2.copy(path = "aar/moduleAar2.aar")
    override val moduleJar1: JarFileInfo
        get() = super.moduleJar1.copy(path = "jar/moduleJar1.jar")
    override val moduleJar2: JarFileInfo
        get() = super.moduleJar2.copy(path = "jar/moduleJar2.jar")
}
