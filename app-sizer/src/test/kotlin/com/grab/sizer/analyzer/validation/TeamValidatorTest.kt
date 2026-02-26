package com.grab.sizer.analyzer.validation

import com.grab.sizer.analyzer.TeamMapping
import org.junit.Test
import org.junit.Assert.*

class TeamValidatorTest {

    private val validator = TeamValidator()

    @Test
    fun testTeamValidatorShouldValidateConsistentTeams() {
        val teamMapping = createMockTeamMapping(
            moduleTeams = setOf("Platform", "Team1"),
            libraryTeams = setOf("Platform", "Team1"),
        )

        val result = validator.validateTeamMapping(teamMapping)

        assertTrue("Should be successful", result is TeamValidationResult.Success)
    }

    @Test
    fun testTeamValidatorShouldValidateInconsistentTeams() {
        val teamMapping = createMockTeamMapping(
            moduleTeams = setOf("Platform", "OnlyInModules"),
            libraryTeams = setOf("Platform", "OnlyInLibraries"),
        )

        val result = validator.validateTeamMapping(teamMapping)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings
        assertEquals("Should have 2 issues", 2, warnings.issues.size)

        val moduleOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInModules }
        assertNotNull("Should detect teams only in modules", moduleOnlyIssue)

        val libraryOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInLibraries }
        assertNotNull("Should detect teams only in libraries", libraryOnlyIssue)
    }

    @Test
    fun testTeamValidatorShouldValidateWithNoLibraryFile() {
        val teamMapping = createMockTeamMapping(
            moduleTeams = setOf("Platform"),
            libraryTeams = emptySet(),
        )

        val result = validator.validateTeamMapping(teamMapping)

        assertTrue("Should be successful", result is TeamValidationResult.Success)
        val success = result as TeamValidationResult.Success
        assertEquals("No libraries configured for validation", success.message)
    }

    @Test
    fun testTeamValidatorShouldReturnSuccessWhenTeamsAreConsistent() {
        val moduleTeams = setOf("Platform", "Team1", "Team2")
        val libraryTeams = setOf("Platform", "Team1", "Team2")

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should be successful", result is TeamValidationResult.Success)
        val success = result as TeamValidationResult.Success
        assertEquals("Team configuration is consistent", success.message)
    }

    @Test
    fun testTeamValidatorShouldDetectTeamsOnlyInModules() {
        val moduleTeams = setOf("Platform", "Team1", "OnlyInModules")
        val libraryTeams = setOf("Platform", "Team1")

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        val moduleOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInModules }
        assertNotNull("Should have teams only in modules issue", moduleOnlyIssue)

        val teamsOnlyInModules = moduleOnlyIssue as ValidationIssue.TeamsOnlyInModules
        assertEquals(setOf("OnlyInModules"), teamsOnlyInModules.teams)
        assertEquals(ValidationSeverity.WARNING, teamsOnlyInModules.severity)
    }

    @Test
    fun testTeamValidatorShouldDetectTeamsOnlyInLibraries() {
        val moduleTeams = setOf("Platform", "Team1")
        val libraryTeams = setOf("Platform", "Team1", "OnlyInLibraries")

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        val libraryOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInLibraries }
        assertNotNull("Should have teams only in libraries issue", libraryOnlyIssue)

        val teamsOnlyInLibraries = libraryOnlyIssue as ValidationIssue.TeamsOnlyInLibraries
        assertEquals(setOf("OnlyInLibraries"), teamsOnlyInLibraries.teams)
        assertEquals(ValidationSeverity.WARNING, teamsOnlyInLibraries.severity)
    }

    @Test
    fun testTeamValidatorShouldDetectSimilarTeamNamesAsTypos() {
        val moduleTeams = setOf("Platform", "Team1")
        val libraryTeams = setOf("Platfrom", "Team1s") // typos

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        val similarNameIssue = warnings.issues.find { it is ValidationIssue.SimilarTeamNames }
        assertNotNull("Should have similar team names issue", similarNameIssue)

        val similarTeamNames = similarNameIssue as ValidationIssue.SimilarTeamNames
        val typos = similarTeamNames.potentialTypos

        assertEquals(2, typos.size)

        val platformTypo = typos.find { it.libraryTeam == "Platfrom" }
        assertNotNull("Should find Platform typo", platformTypo)
        assertEquals("Platform", platformTypo!!.moduleTeam)
        assertTrue("Should have high similarity", platformTypo.similarity > 0.7)

        val team1Typo = typos.find { it.libraryTeam == "Team1s" }
        assertNotNull("Should find Team1 typo", team1Typo)
        assertEquals("Team1", team1Typo!!.moduleTeam)
        assertTrue("Should have high similarity", team1Typo.similarity > 0.7)
    }

    @Test
    fun testTeamValidatorShouldDetectMultipleIssuesAtOnce() {
        val moduleTeams = setOf("Platform", "Team1", "OnlyInModules")
        val libraryTeams = setOf("Platfrom", "OnlyInLibraries") // Platform is a typo

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        assertEquals("Should have 3 issues", 3, warnings.issues.size)

        // Should have teams only in modules
        val moduleOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInModules }
        assertNotNull("Should detect teams only in modules", moduleOnlyIssue)

        // Should have teams only in libraries
        val libraryOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInLibraries }
        assertNotNull("Should detect teams only in libraries", libraryOnlyIssue)

        // Should detect similar names (typos)
        val similarNamesIssue = warnings.issues.find { it is ValidationIssue.SimilarTeamNames }
        assertNotNull("Should detect similar names", similarNamesIssue)
    }

    @Test
    fun testTeamValidatorShouldNotDetectLowSimilarityAsTypos() {
        val moduleTeams = setOf("Platform", "Team1")
        val libraryTeams = setOf("CompletelyDifferent", "AnotherTeam")

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        // Should have 2 issues: teams only in modules AND teams only in libraries, but no similar names
        assertEquals("Should have 2 issues", 2, warnings.issues.size)

        val moduleOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInModules }
        assertNotNull("Should detect teams only in modules", moduleOnlyIssue)

        val libraryOnlyIssue = warnings.issues.find { it is ValidationIssue.TeamsOnlyInLibraries }
        assertNotNull("Should detect teams only in libraries", libraryOnlyIssue)

        val similarNamesIssue = warnings.issues.find { it is ValidationIssue.SimilarTeamNames }
        assertNull("Should not detect similar names for low similarity", similarNamesIssue)
    }

    @Test
    fun testTeamValidatorShouldHandleEmptyTeamSets() {
        val result = validator.validateTeamConsistency(emptySet(), emptySet())

        assertTrue("Should be successful for empty sets", result is TeamValidationResult.Success)
    }

    @Test
    fun testTeamValidatorShouldHandleOneEmptyTeamSet() {
        val moduleTeams = setOf("Platform")
        val libraryTeams = emptySet<String>()

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        assertEquals("Should have 1 issue", 1, warnings.issues.size)
        val moduleOnlyIssue = warnings.issues.first() as ValidationIssue.TeamsOnlyInModules
        assertEquals(setOf("Platform"), moduleOnlyIssue.teams)
    }

    @Test
    fun testTeamValidatorShouldHandleCaseInsensitiveSimilarityCorrectly() {
        val moduleTeams = setOf("PLATFORM")
        val libraryTeams = setOf("platform") // different case

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        assertTrue("Should have warnings", result is TeamValidationResult.Warnings)
        val warnings = result as TeamValidationResult.Warnings

        // Should detect both as library-only and similar names due to case difference
        val similarNamesIssue = warnings.issues.find { it is ValidationIssue.SimilarTeamNames }
        assertNotNull("Should detect similar names (case difference)", similarNamesIssue)

        val similarTeamNames = similarNamesIssue as ValidationIssue.SimilarTeamNames
        val similarity = similarTeamNames.potentialTypos.first()
        assertEquals("platform", similarity.libraryTeam)
        assertEquals("PLATFORM", similarity.moduleTeam)
        assertEquals(1.0, similarity.similarity, 0.001) // Should be identical when lowercased
    }

    @Test
    fun testTeamValidatorShouldCalculateStringSimilarityCorrectly() {
        // Test specific similarity cases with expected Levenshtein distance results
        val moduleTeams = setOf("Platform", "Team1", "LongTeamName")
        val libraryTeams = setOf("Platfrom", "Team1s", "LongTeamNam")

        val result = validator.validateTeamConsistency(moduleTeams, libraryTeams)

        val warnings = result as TeamValidationResult.Warnings
        val similarNamesIssue = warnings.issues.find { it is ValidationIssue.SimilarTeamNames } as ValidationIssue.SimilarTeamNames
        val typos = similarNamesIssue.potentialTypos

        // Platform -> Platfrom: 2 character substitutions, similarity = (8-2)/8 = 0.75
        val platformTypo = typos.find { it.libraryTeam == "Platfrom" }
        assertNotNull("Should find Platform typo", platformTypo)
        assertEquals("Platform typo similarity should be 0.75", 0.75, platformTypo!!.similarity, 0.001)

        // Team1 -> Team1s: 1 character addition, similarity = (6-1)/6 ≈ 0.833
        val team1Typo = typos.find { it.libraryTeam == "Team1s" }
        assertNotNull("Should find Team1 typo", team1Typo)
        assertEquals("Team1 typo similarity should be ~0.833", 5.0/6.0, team1Typo!!.similarity, 0.001)

        // LongTeamName -> LongTeamNam: 1 character deletion, similarity = (12-1)/12 ≈ 0.917
        val longNameTypo = typos.find { it.libraryTeam == "LongTeamNam" }
        assertNotNull("Should find LongTeamName typo", longNameTypo)
        assertEquals("LongTeamName typo similarity should be ~0.917", 11.0/12.0, longNameTypo!!.similarity, 0.001)
    }

    // Helper method to create mock TeamMapping
    private fun createMockTeamMapping(
        moduleTeams: Set<String>,
        libraryTeams: Set<String>,
    ): TeamMapping {
        return object : TeamMapping {
            override fun getModuleOwner(moduleName: String): String? = null
            override fun getLibraryOwner(libraryCoordinate: String): String? = null
            override fun getAllTeams(): Set<String> = moduleTeams + libraryTeams

            override fun getModuleTeams(): Set<String> = moduleTeams
            override fun getLibraryTeams(): Set<String> = libraryTeams
        }
    }
}
