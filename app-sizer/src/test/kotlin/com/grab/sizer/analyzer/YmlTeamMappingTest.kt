package com.grab.sizer.analyzer

import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

class YmlTeamMappingTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testTeamToModuleMapping() {
        val ymlContent = """
            Team1:
              - :module1
              - :group1:module2
            Team2:
              - :module3
              - :group2:module4
        """.trimIndent()

        val ymlFile = tempFolder.newFile("team_mapping.yml").apply {
            writeText(ymlContent)
        }

        val teamMapping = YmlTeamMapping(ymlFile)

        assertEquals(2, teamMapping.teamToModuleMap.size)
        assertEquals(listOf("module1", "group1:module2"), teamMapping.teamToModuleMap["Team1"])
        assertEquals(listOf("module3", "group2:module4"), teamMapping.teamToModuleMap["Team2"])
    }

    @Test
    fun testModuleToTeamMapping() {
        val ymlContent = """
            Team1:
              - :module1
              - :group1:module2
            Team2:
              - :module3
              - :group2:module4
        """.trimIndent()

        val ymlFile = tempFolder.newFile("team_mapping.yml").apply {
            writeText(ymlContent)
        }

        val teamMapping = YmlTeamMapping(ymlFile)

        assertEquals(4, teamMapping.moduleToTeamMap.size)
        assertEquals("Team1", teamMapping.moduleToTeamMap["module1"])
        assertEquals("Team1", teamMapping.moduleToTeamMap["group1:module2"])
        assertEquals("Team2", teamMapping.moduleToTeamMap["module3"])
        assertEquals("Team2", teamMapping.moduleToTeamMap["group2:module4"])
    }

    @Test
    fun testYamlFileWithNoModules() {
        val ymlContent = """
            Team1:
            Team2:
        """.trimIndent()

        val ymlFile = tempFolder.newFile("no_modules_mapping.yml").apply {
            writeText(ymlContent)
        }

        val teamMapping = YmlTeamMapping(ymlFile)

        assertEquals(2, teamMapping.teamToModuleMap.size)
        assertTrue(teamMapping.teamToModuleMap["Team1"]?.isEmpty() ?: false)
        assertTrue(teamMapping.teamToModuleMap["Team2"]?.isEmpty() ?: false)
        assertTrue(teamMapping.moduleToTeamMap.isEmpty())
    }

    @Test
    fun testTrimmingOfColonInModuleNames() {
        val ymlContent = """
            Team1:
              - :module1
              - :group1:module2
            Team2:
              - :module3
              - :group2:module4
        """.trimIndent()

        val ymlFile = tempFolder.newFile("trim_colon_mapping.yml").apply {
            writeText(ymlContent)
        }

        val teamMapping = YmlTeamMapping(ymlFile)
        assertEquals(listOf("module1", "group1:module2"), teamMapping.teamToModuleMap["Team1"])
        assertEquals("Team1", teamMapping.moduleToTeamMap["module1"])
        assertEquals("Team1", teamMapping.moduleToTeamMap["group1:module2"])
    }

    @Test(expected = Exception::class)
    fun testNonExistentFileThrowsException() {
        val nonExistentFile = File("non_existent_file.yml")
        YmlTeamMapping(nonExistentFile).teamToModuleMap
    }
}