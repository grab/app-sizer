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

import com.grab.plugin.sizer.utils.assertThrows
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DefaultVariantExtractorTest {

    private lateinit var rootProject: Project
    private lateinit var variantInput: VariantInput
    private lateinit var extractor: DefaultVariantExtractor

    @Before
    fun setup() {
        rootProject = TestProjectCreator.createRootProject()
        variantInput = VariantInput("flavor1Debug", "flavor1", "debug", "1.0")
        extractor = DefaultVariantExtractor(
            variantInput,
            listOf("flavor1", "flavor2"),
            listOf("debug", "release"),
            false
        )
    }

    @Test
    fun `findMatchVariant for Android library`() {
        val project = TestProjectCreator.createAndroidLibraryProject(rootProject, "library")
        val result = extractor.findMatchVariant(project)
        assertAndroidVariant(result, "debug", "flavor1")
    }

    @Test
    fun `findMatchVariant for Android application`() {
        val project = TestProjectCreator.createAndroidAppProject(rootProject, "app")
        val result = extractor.findMatchVariant(project)
        assertAndroidVariant(result, "debug", "flavor1")
    }

    @Test
    fun `findMatchVariant for Java project`() {
        val project = TestProjectCreator.createJavaProject(rootProject, "java-project")
        val result = extractor.findMatchVariant(project)
        Assert.assertTrue(result is JarAppSizeVariant)
    }


    @Test
    fun `findMatchVariant for unsupported project type`() {
        val project = ProjectBuilder.builder().withParent(rootProject).build()
        project.doEvaluate()

        assertThrows<IllegalArgumentException>("${project.name} is not supported") {
            extractor.findMatchVariant(project)
        }
    }

    @Test
    fun `findMatchVariant with enableMatchDebugVariant true for library`() {
        val project = TestProjectCreator.createAndroidLibraryProject(rootProject, "lib")
        val variantInput = VariantInput("flavor1Release", "flavor1", "release", "1.0")
        val extractorWithDebugEnabled =
            DefaultVariantExtractor(variantInput, listOf("flavor1", "flavor2"), listOf("debug", "release"), true)
        val result = extractorWithDebugEnabled.findMatchVariant(project)
        assertAndroidVariant(result, "debug", "flavor1")
    }

    @Test
    fun `extractVariant returns full match when available for library`() {
        val project =
            TestProjectCreator.createAndroidLibraryProject(rootProject, "lib", flavors = listOf("flavor1", "flavor2"))
        val variantInput = VariantInput("flavor1Release", "flavor1", "release", "1.0")
        val extractor = createExtractor(variantInput)
        val result = extractor.findMatchVariant(project)

        assertAndroidVariant(result, "release", "flavor1")
    }

    @Test
    fun `extractVariant matches flavor and build type separately when no full match for library`() {
        val project =
            TestProjectCreator.createAndroidLibraryProject(rootProject, "lib", flavors = listOf("flavor1", "flavor2"))
        val variantInput = VariantInput("flavor2Release", "flavor2", "release", "1.0")
        val extractor = createExtractor(variantInput)

        val result = extractor.findMatchVariant(project)

        assertAndroidVariant(result, "release", "flavor2")
    }

    @Test
    fun `extractVariant uses build type fallback when flavor matches but build type doesn't for library`() {
        val project =
            TestProjectCreator.createAndroidLibraryProject(rootProject, "lib", flavors = listOf("flavor1", "flavor2"))
        val variantInput = VariantInput("flavor1CustomBuildType", "flavor1", "customBuildType", "1.0")
        val extractor = createExtractor(variantInput, buildTypeFallbacks = listOf("release", "debug"))

        val result = extractor.findMatchVariant(project)

        assertAndroidVariant(result, "release", "flavor1")
    }

    @Test
    fun `extractVariant uses flavor fallback when build type matches but flavor doesn't for library`() {
        val project =
            TestProjectCreator.createAndroidLibraryProject(rootProject, "lib", flavors = listOf("flavor1", "flavor2"))
        val variantInput = VariantInput("customFlavorDebug", "customFlavor", "debug", "1.0")
        val extractor = createExtractor(variantInput, flavorFallbacks = listOf("flavor2", "flavor1"))

        val result = extractor.findMatchVariant(project)

        assertAndroidVariant(result, "debug", "flavor2")
    }

    @Test
    fun `extractVariant falls back to debug when no match found for library`() {
        val project =
            TestProjectCreator.createAndroidLibraryProject(rootProject, "lib", flavors = listOf("flavor1", "flavor2"))
        val variantInput = VariantInput("customFlavorCustomBuildType", "customFlavor", "customBuildType", "1.0")
        val extractor = createExtractor(variantInput)

        val result = extractor.findMatchVariant(project)

        assertAndroidVariant(result, "debug", "flavor1")
    }

    private fun createExtractor(
        variantInput: VariantInput,
        flavorFallbacks: List<String> = listOf("flavor1", "flavor2"),
        buildTypeFallbacks: List<String> = listOf("debug", "release")
    ): DefaultVariantExtractor {
        return DefaultVariantExtractor(
            variantInput,
            flavorFallbacks,
            buildTypeFallbacks,
            false
        )
    }

    private fun assertAndroidVariant(result: AppSizeVariant, expectedBuildType: String, expectedFlavor: String) {
        Assert.assertTrue(result is AndroidAppSizeVariant)
        Assert.assertEquals(expectedBuildType, result.buildType)
        Assert.assertEquals(expectedFlavor, result.buildFlavor)
    }
}