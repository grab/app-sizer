package com.grab.sizer.analyzer

import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Tests for YmlTeamMapping as a pure data provider.
 * Validation logic is now tested separately in TeamValidatorTest.
 */
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

        val teamMapping = YmlTeamMapping(ymlFile, null)

        // Test core mapping functionality
        assertEquals("Team1", teamMapping.getModuleOwner("module1"))
        assertEquals("Team1", teamMapping.getModuleOwner("group1:module2"))
        assertEquals("Team2", teamMapping.getModuleOwner("module3"))
        assertEquals("Team2", teamMapping.getModuleOwner("group2:module4"))
        assertEquals(setOf("Team1", "Team2"), teamMapping.getAllTeams())

        // Test data provider methods
        assertEquals(setOf("Team1", "Team2"), teamMapping.getModuleTeams())
        assertEquals(emptySet<String>(), teamMapping.getLibraryTeams())
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

        val teamMapping = YmlTeamMapping(ymlFile, null)

        assertEquals("Team1", teamMapping.getModuleOwner("module1"))
        assertEquals("Team1", teamMapping.getModuleOwner("group1:module2"))
        assertEquals("Team2", teamMapping.getModuleOwner("module3"))
        assertEquals("Team2", teamMapping.getModuleOwner("group2:module4"))
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

        val teamMapping = YmlTeamMapping(ymlFile, null)

        assertEquals(setOf("Team1", "Team2"), teamMapping.getAllTeams())
        assertEquals(setOf("Team1", "Team2"), teamMapping.getModuleTeams())
        assertNull(teamMapping.getModuleOwner("nonexistent"))
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

        val teamMapping = YmlTeamMapping(ymlFile, null)
        assertEquals("Team1", teamMapping.getModuleOwner("module1"))
        assertEquals("Team1", teamMapping.getModuleOwner("group1:module2"))
    }

    @Test(expected = Exception::class)
    fun testNonExistentFileThrowsException() {
        val nonExistentFile = File("non_existent_file.yml")
        YmlTeamMapping(nonExistentFile, null).getAllTeams()
    }

    // Library ownership pattern matching tests

    @Test
    fun testLibraryOwnershipExactMatch() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                Platform:
                  - androidx.core:core:1.8.0
                  - com.google.android:material:1.6.1
                Networking:
                  - com.squareup.okhttp3:okhttp:4.9.3
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test exact matches
        assertEquals("Platform", teamMapping.getLibraryOwner("androidx.core:core:1.8.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("com.google.android:material:1.6.1"))
        assertEquals("Networking", teamMapping.getLibraryOwner("com.squareup.okhttp3:okhttp:4.9.3"))

        // Test data provider methods
        assertEquals(setOf("Team1"), teamMapping.getModuleTeams())
        assertEquals(setOf("Platform", "Networking"), teamMapping.getLibraryTeams())
        assertEquals(setOf("Team1", "Platform", "Networking"), teamMapping.getAllTeams())
    }

    @Test
    fun testLibraryOwnershipArtifactWildcard() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                Platform:
                  - androidx.core:*
                  - com.google.android:material:*
                Networking:
                  - com.squareup.okhttp3:*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test artifact wildcard matches
        assertEquals("Platform", teamMapping.getLibraryOwner("androidx.core:core:1.8.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("androidx.core:core-ktx:1.8.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("com.google.android:material:1.6.1"))
        assertEquals("Networking", teamMapping.getLibraryOwner("com.squareup.okhttp3:okhttp:4.9.3"))
        assertEquals("Networking", teamMapping.getLibraryOwner("com.squareup.okhttp3:logging-interceptor:4.9.3"))
    }

    @Test
    fun testLibraryOwnershipGroupWildcard() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                Platform:
                  - androidx.*
                  - com.google.android.*
                Networking:
                  - com.squareup.*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test group wildcard matches
        assertEquals("Platform", teamMapping.getLibraryOwner("androidx.core:core:1.8.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("androidx.fragment:fragment:1.5.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("com.google.android.material:material:1.6.1"))
        assertEquals("Networking", teamMapping.getLibraryOwner("com.squareup.okhttp3:okhttp:4.9.3"))
        assertEquals("Networking", teamMapping.getLibraryOwner("com.squareup.retrofit2:retrofit:2.9.0"))
    }

    @Test
    fun testLibraryOwnershipPatternPriority() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                ExactTeam:
                  - com.example:library:1.0.0
                ArtifactTeam:
                  - com.example:library:*
                GroupTeam:
                  - com.example.*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Exact match should win over wildcards
        assertEquals("ExactTeam", teamMapping.getLibraryOwner("com.example:library:1.0.0"))

        // Artifact wildcard should win over group wildcard for different versions
        assertEquals("ArtifactTeam", teamMapping.getLibraryOwner("com.example:library:2.0.0"))

        // Group wildcard should match different artifacts
        assertEquals("GroupTeam", teamMapping.getLibraryOwner("com.example:other:1.0.0"))
    }

    @Test
    fun testLibraryOwnershipNoMatch() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                Platform:
                  - androidx.*
                Networking:
                  - com.squareup.*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test coordinates that don't match any patterns
        assertNull(teamMapping.getLibraryOwner("com.unknown:library:1.0.0"))
        assertNull(teamMapping.getLibraryOwner("org.jetbrains:annotations:23.0.0"))
        assertNull(teamMapping.getLibraryOwner(""))
    }

    @Test
    fun testLibraryOwnershipWithoutLibraryFile() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }

        val teamMapping = YmlTeamMapping(moduleYml, null)

        // Should return null for any library coordinate when no library file provided
        assertNull(teamMapping.getLibraryOwner("androidx.core:core:1.8.0"))
        assertNull(teamMapping.getLibraryOwner("com.squareup.okhttp3:okhttp:4.9.3"))

        // Data provider methods
        assertEquals(setOf("Team1"), teamMapping.getModuleTeams())
        assertEquals(emptySet<String>(), teamMapping.getLibraryTeams())
    }

    @Test
    fun testLibraryOwnershipComplexPatterns() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                Platform:
                  - androidx.*
                  - com.google.*
                  - org.jetbrains.kotlin:*
                UI:
                  - com.github.bumptech.glide:*
                  - com.facebook.fresco:*
                Networking:
                  - com.squareup.*
                  - io.reactivex.*
                  - com.jakewharton.retrofit:*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test various complex coordinates
        assertEquals("Platform", teamMapping.getLibraryOwner("androidx.appcompat:appcompat:1.5.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("com.google.firebase:firebase-core:21.0.0"))
        assertEquals("Platform", teamMapping.getLibraryOwner("org.jetbrains.kotlin:kotlin-stdlib:1.7.10"))

        assertEquals("UI", teamMapping.getLibraryOwner("com.github.bumptech.glide:glide:4.13.2"))
        assertEquals("UI", teamMapping.getLibraryOwner("com.facebook.fresco:fresco:2.6.0"))

        assertEquals("Networking", teamMapping.getLibraryOwner("com.squareup.okhttp3:okhttp:4.9.3"))
        assertEquals("Networking", teamMapping.getLibraryOwner("io.reactivex.rxjava2:rxjava:2.2.21"))
        assertEquals("Networking", teamMapping.getLibraryOwner("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"))
    }

    @Test
    fun testLibraryOwnershipSimple() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("Team1:\n  - :module1")
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                TestTeam:
                  - com.example.*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test basic group wildcard
        val result = teamMapping.getLibraryOwner("com.example.library:artifact:1.0.0")
        assertEquals("TestTeam", result)

        // Test non-matching
        assertNull(teamMapping.getLibraryOwner("com.other:library:1.0.0"))
    }

    @Test
    fun testDataProviderMethods() {
        val moduleYml = tempFolder.newFile("modules.yml").apply {
            writeText("""
                Platform:
                  - app
                Team1:
                  - module1
            """.trimIndent())
        }
        val libraryYml = tempFolder.newFile("libraries.yml").apply {
            writeText("""
                Platform:
                  - androidx.*
                LibraryOnlyTeam:
                  - com.example.*
            """.trimIndent())
        }

        val teamMapping = YmlTeamMapping(moduleYml, libraryYml)

        // Test data provider methods
        assertEquals(setOf("Platform", "Team1"), teamMapping.getModuleTeams())
        assertEquals(setOf("Platform", "LibraryOnlyTeam"), teamMapping.getLibraryTeams())
        assertEquals(setOf("Platform", "Team1", "LibraryOnlyTeam"), teamMapping.getAllTeams())
    }
}
