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

package com.grab.plugin.sizer.fake

import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.specs.Spec
import org.gradle.internal.component.AmbiguousVariantSelectionException
import java.io.File

class FakeResolvedConfiguration(
    private val dependencies: List<Dependency>,
    private val throwAmbiguousException: Boolean = false
) : ResolvedConfiguration {
    override fun getFirstLevelModuleDependencies(): Set<ResolvedDependency> {
        return dependencies.map { dep ->
            FakeResolvedDependency(
                throwAmbiguousException = throwAmbiguousException,
                dependency = dep
            )
        }.toSet()
    }

    override fun hasError(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLenientConfiguration(): LenientConfiguration {
        TODO("Not yet implemented")
    }

    override fun rethrowFailure() {
        TODO("Not yet implemented")
    }

    override fun getFiles(): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun getFiles(dependencySpec: Spec<in Dependency>): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun getFirstLevelModuleDependencies(dependencySpec: Spec<in Dependency>): MutableSet<ResolvedDependency> {
        TODO("Not yet implemented")
    }

    override fun getResolvedArtifacts(): MutableSet<ResolvedArtifact> {
        TODO("Not yet implemented")
    }
}

class FakeResolvedArtifact(private val dependency: Dependency) : ResolvedArtifact {
    override fun getFile(): File {
        return File("/path/to/${dependency.name}.aar")
    }

    override fun getModuleVersion(): ResolvedModuleVersion {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getType(): String {
        TODO("Not yet implemented")
    }

    override fun getExtension(): String? {
        TODO("Not yet implemented")
    }

    override fun getClassifier(): String? {
        TODO("Not yet implemented")
    }

    override fun getId(): ComponentArtifactIdentifier {
        return object : ComponentArtifactIdentifier {
            override fun getComponentIdentifier(): ComponentIdentifier {
                return object :ComponentIdentifier{
                    override fun getDisplayName(): String = dependency.name
                    override fun toString(): String = dependency.name
                }
            }

            override fun getDisplayName(): String = dependency.name
        }
    }
}

class FakeResolvedDependency(
    private val throwAmbiguousException: Boolean,
    private val dependency: Dependency
) : ResolvedDependency {
    override fun getModuleVersion(): String = dependency.version ?: "unspecified"
    override fun getAllModuleArtifacts(): Set<ResolvedArtifact> {
        if (throwAmbiguousException) {
            throw FakeAmbiguousVariantSelectionException()
        }

        return setOf(FakeResolvedArtifact(dependency))
    }

    override fun getModuleGroup(): String = dependency.group ?: ""

    override fun getName(): String = dependency.name

    override fun getModuleName(): String {
        TODO("Not yet implemented")
    }

    override fun getConfiguration(): String {
        TODO("Not yet implemented")
    }

    override fun getModule(): ResolvedModuleVersion {
        TODO("Not yet implemented")
    }

    override fun getChildren(): MutableSet<ResolvedDependency> {
        TODO("Not yet implemented")
    }

    override fun getParents(): MutableSet<ResolvedDependency> {
        TODO("Not yet implemented")
    }

    override fun getModuleArtifacts(): MutableSet<ResolvedArtifact> {
        TODO("Not yet implemented")
    }

    override fun getParentArtifacts(parent: ResolvedDependency): MutableSet<ResolvedArtifact> {
        TODO("Not yet implemented")
    }

    override fun getArtifacts(parent: ResolvedDependency): MutableSet<ResolvedArtifact> {
        TODO("Not yet implemented")
    }

    override fun getAllArtifacts(parent: ResolvedDependency): MutableSet<ResolvedArtifact> {
        TODO("Not yet implemented")
    }
}

class FakeAmbiguousVariantSelectionException() : AmbiguousVariantSelectionException("Fake Exception")