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

import org.gradle.api.artifacts.component.BuildIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArtifactMappingTest {

    @Test
    fun `project component with aar artifact maps to a module dependency`() {
        val dependency = toArchiveDependency(
            projectComponent(":sample-group:android-module-level2"),
            File("/build/outputs/aar/android-module-level2-debug.aar"),
            ARTIFACT_TYPE_AAR,
        )

        assertTrue(dependency is ModuleDependency)
        assertEquals("sample-group:android-module-level2", dependency?.name)
        assertEquals("/build/outputs/aar/android-module-level2-debug.aar", dependency?.pathToArtifact)
    }

    @Test
    fun `project component with jar artifact maps to a java module dependency`() {
        val dependency = toArchiveDependency(
            projectComponent(":kotlin-module"),
            File("/build/libs/kotlin-module.jar"),
            ARTIFACT_TYPE_JAR,
        )

        assertTrue(dependency is JavaModuleDependency)
        assertEquals("kotlin-module", dependency?.name)
    }

    @Test
    fun `external component maps to an external dependency named by its coordinates`() {
        val dependency = toArchiveDependency(
            moduleComponent("com.google.guava", "guava", "33.2.1-jre"),
            File("/cache/guava-33.2.1-jre.jar"),
            ARTIFACT_TYPE_JAR,
        )

        assertTrue(dependency is ExternalDependency)
        assertEquals("com.google.guava:guava:33.2.1-jre", dependency?.name)
    }

    @Test
    fun `unattributable component is skipped`() {
        val opaque = object : ComponentIdentifier {
            override fun getDisplayName(): String = "files/local.jar"
        }

        assertNull(toArchiveDependency(opaque, File("/local.jar"), ARTIFACT_TYPE_JAR))
    }

    private fun projectComponent(path: String): ProjectComponentIdentifier =
        object : ProjectComponentIdentifier {
            override fun getDisplayName(): String = "project $path"
            override fun getBuild(): BuildIdentifier = throw UnsupportedOperationException()
            override fun getProjectPath(): String = path
            override fun getBuildTreePath(): String = path
            override fun getProjectName(): String = path.substringAfterLast(':')
        }

    private fun moduleComponent(group: String, name: String, version: String): ModuleComponentIdentifier =
        object : ModuleComponentIdentifier {
            override fun getDisplayName(): String = "$group:$name:$version"
            override fun getGroup(): String = group
            override fun getModule(): String = name
            override fun getVersion(): String = version
            override fun getModuleIdentifier(): ModuleIdentifier = throw UnsupportedOperationException()
        }
}
