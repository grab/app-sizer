/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.plugin.sizer.dependencies

import com.grab.plugin.sizer.fake.FakeConfiguration
import com.grab.plugin.sizer.fake.MockLogger
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DefaultDependencyExtractorTest {
    private lateinit var testProject: TestProject
    private lateinit var mockLogger: MockLogger
    private lateinit var archiveExtractor: ArchiveExtractor

    @Before
    fun setup() {
        testProject = TestProject()
        mockLogger = MockLogger()
        archiveExtractor = createMockArchiveExtractor()
    }

    @Test
    fun `extract method successfully extracts internal dependencies`() {
        val configurationExtractor = testProject.createConfigurationExtractor()
        val extractor = DefaultDependencyExtractor(testProject.appModule, configurationExtractor, archiveExtractor, mockLogger)
        val result = extractor.extract()

        assertTrue(result.any { it is ModuleDependency && it.name == testProject.module1.name })
        assertTrue(result.any { it is ModuleDependency && it.name == testProject.module2.name })
    }

    @Test
    fun `extract method successfully extracts external dependencies`() {
        val configurationExtractor = testProject.createConfigurationExtractor()
        val extractor = DefaultDependencyExtractor(testProject.appModule, configurationExtractor, archiveExtractor, mockLogger)
        val result = extractor.extract()

        testProject.externalLibs.forEach { lib ->
            assertTrue(result.any { it is ExternalDependency && it.name.contains(lib.split(":")[1]) })
        }
    }

    @Test
    fun `extract method handles non-resolvable configurations`() {
        val nonResolvableConfig = testProject.createNonResolvableConfiguration(testProject.appModule)
        val configExtractor = MockConfigurationExtractor(
            mapOf(testProject.appModule to listOf(nonResolvableConfig))
        )
        val extractor = DefaultDependencyExtractor(testProject.appModule, configExtractor, archiveExtractor, mockLogger)
        val result = extractor.extract()

        assertEquals(0, result.filterIsInstance<ExternalDependency>().size)
    }

    @Test
    fun `extract method handles AmbiguousVariantSelectionException`() {
        val ambiguousConfig = testProject.createAmbiguousConfiguration(testProject.appModule)
        val configExtractor = MockConfigurationExtractor(
            mapOf(testProject.appModule to listOf(ambiguousConfig))
        )
        val extractor = DefaultDependencyExtractor(testProject.appModule, configExtractor, archiveExtractor, mockLogger)
        val result = extractor.extract()

        assertEquals(1, result.size) // App module
        assertTrue(mockLogger.loggedMessages.any { it.contains("Fetching allModuleArtifacts having issue with") })
    }

    @Test
    fun `extract method handles dependencies with unspecified version`() {
        val mixedConfig = testProject.createVersionedModuleConfiguration(testProject.appModule)
        val configExtractor = MockConfigurationExtractor(
            mapOf(testProject.appModule to listOf(mixedConfig))
        )

        val extractor = DefaultDependencyExtractor(testProject.appModule, configExtractor, archiveExtractor, mockLogger)
        val result = extractor.extract()
        assertEquals(2, result.filterIsInstance<ModuleDependency>().size) // app + module1
        assertEquals(1, result.filterIsInstance<ExternalDependency>().size)
    }

    private fun createMockArchiveExtractor(): ArchiveExtractor {
        return object : ArchiveExtractor {
            override fun extract(project: Project): ArchiveDependency {
                return ModuleDependency(project.name, "/path/to/${project.name}.aar")
            }
        }
    }
}


private class TestProject {
    val rootProject = ProjectBuilder.builder().withName("root").build()

    val appModule = ProjectBuilder.builder().withName("app").withParent(rootProject).build()
    val module1 = ProjectBuilder.builder().withName("sub1").withParent(rootProject).build()
    val module2 = ProjectBuilder.builder().withName("sub2").withParent(rootProject).build()

    val externalLibs = listOf(
        "com.example:lib1:2.0.0",
        "com.example:lib2:2.0.0",
        "com.example:lib3:2.0.0",
        "com.example:lib4:2.0.0",
        "com.example:lib5:2.0.0"
    )

    fun createAppDependencies() = createDependencies(appModule, module1, module2, externalLibs[0], externalLibs[1])
    fun createModule1Dependencies() = createDependencies(module1, module2, externalLibs[2])
    fun createModule2Dependencies() = createDependencies(module2, externalLibs[3], externalLibs[4])

    fun createAppConfiguration() = FakeConfiguration(createAppDependencies())
    fun createModule1Configuration() = FakeConfiguration(createModule1Dependencies())
    fun createModule2Configuration() = FakeConfiguration(createModule2Dependencies())

    fun createConfigurationExtractor() = MockConfigurationExtractor(
        mapOf(
            appModule to listOf(createAppConfiguration()),
            module1 to listOf(createModule1Configuration()),
            module2 to listOf(createModule2Configuration())
        )
    )

    fun createDependency(project: Project, notation: Any): Dependency = when (notation) {
        is Project -> project.dependencies.project(mapOf("path" to notation.path))
        is String -> project.dependencies.create(notation)
        else -> throw IllegalArgumentException("Unsupported dependency type: ${notation::class}")
    }

    private fun createDependencies(project: Project, vararg dependencies: Any): List<Dependency> =
        dependencies.map { createDependency(project, it) }

    fun createExternalDependency(project: Project, notation: String): Dependency =
        project.dependencies.create(notation)

    fun createProjectDependency(project: Project, dependencyProject: Project): ProjectDependency =
        project.dependencies.project(mapOf("path" to dependencyProject.path)) as ProjectDependency

    fun createNonResolvableConfiguration(project: Project): Configuration =
        FakeConfiguration(createDependencies(project, externalLibs[0], externalLibs[1]), isResolvable = false)

    fun createAmbiguousConfiguration(project: Project): Configuration =
        FakeConfiguration(createDependencies(project, externalLibs[0], externalLibs[1]), throwAmbiguousException = true)

    fun createVersionedModuleConfiguration(project: Project): Configuration {
        module1.version = "1.0.0"
        val versionedModule = createProjectDependency(project, module1)
        val externalLib = createExternalDependency(project, "com.example:lib:0.0.1")
        return FakeConfiguration(listOf(versionedModule, externalLib))
    }
}


class MockConfigurationExtractor(private val configurations: Map<Project, List<Configuration>>) :
    ConfigurationExtractor {
    override fun runtimeConfigurations(project: Project): Sequence<Configuration> {
        return configurations[project]?.asSequence() ?: emptySequence()
    }
}