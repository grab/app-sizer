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

import com.grab.plugin.sizer.utils.isAndroidApplication
import com.grab.plugin.sizer.utils.isAndroidLibrary
import com.grab.plugin.sizer.utils.isJava
import com.grab.plugin.sizer.utils.isKotlinJvm
import com.grab.plugin.sizer.utils.isKotlinMultiplatform
import org.gradle.api.Project
import javax.inject.Inject

interface ArchiveExtractor {
    /**
     * Extracts archive dependency from a project.
     * 
     * @param project The project to extract archive dependency from
     * @return ArchiveDependency representing the project's binary output
     * @throws UnsupportedOperationException if the project type is unsupported
     * @throws IllegalStateException if variant extraction fails
     */
    @Throws(UnsupportedOperationException::class, IllegalStateException::class)
    fun extract(project: Project): ArchiveDependency
}

@DependenciesScope
internal class DefaultArchiveExtractor @Inject constructor(
    private val variantExtractor: VariantExtractor
) : ArchiveExtractor {
    @Throws(UnsupportedOperationException::class, IllegalStateException::class)
    override fun extract(project: Project): ArchiveDependency {
        val matchVariant = variantExtractor.findMatchVariant(project)
        return when {
            project.isAndroidApplication -> AppDependency(
               name = project.pathTrimColon,
               pathToArtifact = matchVariant.binaryOutPut.path
           )

            project.isAndroidLibrary -> ModuleDependency(
               name = project.pathTrimColon,
               pathToArtifact = matchVariant.binaryOutPut.path
           )

            project.isKotlinJvm || project.isJava || project.isKotlinMultiplatform -> JavaModuleDependency(
               name = project.pathTrimColon,
               pathToArtifact = matchVariant.binaryOutPut.path
           )

            else -> throw UnsupportedOperationException("Unsupported project type: ${project.name}")
        }
    }
}

private val Project.pathTrimColon
    get() = path.trim(':')