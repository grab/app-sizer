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

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency
import java.io.File

class FakeConfiguration(
    private val dependencies: List<Dependency>,
    private val isResolvable: Boolean = true,
    private val throwAmbiguousException: Boolean = false
) : Configuration {
    private val projectDependencies = dependencies.filterIsInstance<ProjectDependency>()
    override fun getName(): String = "runtimeClasspath"
    override fun isCanBeResolved(): Boolean = isResolvable

    override fun getResolvedConfiguration() = FakeResolvedConfiguration(dependencies, throwAmbiguousException)
    override fun getDependencies(): DependencySet = FakeDependencySet(dependencies = projectDependencies.toSet())

    override fun iterator(): MutableIterator<File> {
        TODO("Not yet implemented")
    }

    override fun addToAntBuilder(builder: Any, nodeName: String, type: FileCollection.AntType) {
        TODO("Not yet implemented")
    }

    override fun addToAntBuilder(builder: Any, nodeName: String): Any {
        TODO("Not yet implemented")
    }

    override fun getBuildDependencies(): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun getSingleFile(): File {
        TODO("Not yet implemented")
    }

    override fun getFiles(): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun contains(file: File): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAsPath(): String {
        TODO("Not yet implemented")
    }

    override fun plus(collection: FileCollection): FileCollection {
        TODO("Not yet implemented")
    }

    override fun minus(collection: FileCollection): FileCollection {
        TODO("Not yet implemented")
    }

    override fun filter(filterClosure: Closure<*>): FileCollection {
        TODO("Not yet implemented")
    }

    override fun filter(filterSpec: Spec<in File>): FileCollection {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAsFileTree(): FileTree {
        TODO("Not yet implemented")
    }

    override fun getElements(): Provider<MutableSet<FileSystemLocation>> {
        TODO("Not yet implemented")
    }

    override fun getAttributes(): AttributeContainer {
        TODO("Not yet implemented")
    }

    override fun attributes(action: Action<in AttributeContainer>): Configuration {
        TODO("Not yet implemented")
    }

    override fun getResolutionStrategy(): ResolutionStrategy {
        TODO("Not yet implemented")
    }

    override fun resolutionStrategy(closure: Closure<*>): Configuration {
        TODO("Not yet implemented")
    }

    override fun resolutionStrategy(action: Action<in ResolutionStrategy>): Configuration {
        TODO("Not yet implemented")
    }

    override fun getState(): Configuration.State {
        TODO("Not yet implemented")
    }

    override fun isVisible(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVisible(visible: Boolean): Configuration {
        TODO("Not yet implemented")
    }

    override fun getExtendsFrom(): MutableSet<Configuration> {
        TODO("Not yet implemented")
    }

    override fun setExtendsFrom(superConfigs: MutableIterable<Configuration>): Configuration {
        TODO("Not yet implemented")
    }

    override fun extendsFrom(vararg superConfigs: Configuration?): Configuration {
        TODO("Not yet implemented")
    }

    override fun isTransitive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setTransitive(t: Boolean): Configuration {
        TODO("Not yet implemented")
    }

    override fun getDescription(): String? {
        TODO("Not yet implemented")
    }

    override fun setDescription(description: String?): Configuration {
        TODO("Not yet implemented")
    }

    override fun getHierarchy(): MutableSet<Configuration> {
        TODO("Not yet implemented")
    }

    override fun resolve(): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun files(dependencySpecClosure: Closure<*>): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun files(dependencySpec: Spec<in Dependency>): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun files(vararg dependencies: Dependency?): MutableSet<File> {
        TODO("Not yet implemented")
    }

    override fun fileCollection(dependencySpec: Spec<in Dependency>): FileCollection {
        TODO("Not yet implemented")
    }

    override fun fileCollection(dependencySpecClosure: Closure<*>): FileCollection {
        TODO("Not yet implemented")
    }

    override fun fileCollection(vararg dependencies: Dependency?): FileCollection {
        TODO("Not yet implemented")
    }

    override fun getUploadTaskName(): String {
        TODO("Not yet implemented")
    }

    override fun getTaskDependencyFromProjectDependency(useDependedOn: Boolean, taskName: String): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun getAllDependencies(): DependencySet {
        TODO("Not yet implemented")
    }

    override fun getDependencyConstraints(): DependencyConstraintSet {
        TODO("Not yet implemented")
    }

    override fun getAllDependencyConstraints(): DependencyConstraintSet {
        TODO("Not yet implemented")
    }

    override fun getArtifacts(): PublishArtifactSet {
        TODO("Not yet implemented")
    }

    override fun getAllArtifacts(): PublishArtifactSet {
        TODO("Not yet implemented")
    }

    override fun getExcludeRules(): MutableSet<ExcludeRule> {
        TODO("Not yet implemented")
    }

    override fun exclude(excludeProperties: MutableMap<String, String>): Configuration {
        TODO("Not yet implemented")
    }

    override fun defaultDependencies(action: Action<in DependencySet>): Configuration {
        TODO("Not yet implemented")
    }

    override fun withDependencies(action: Action<in DependencySet>): Configuration {
        TODO("Not yet implemented")
    }

    override fun getAll(): MutableSet<Configuration> {
        TODO("Not yet implemented")
    }

    override fun getIncoming(): ResolvableDependencies {
        TODO("Not yet implemented")
    }

    override fun getOutgoing(): ConfigurationPublications {
        TODO("Not yet implemented")
    }

    override fun outgoing(action: Action<in ConfigurationPublications>) {
        TODO("Not yet implemented")
    }

    override fun copy(): Configuration {
        TODO("Not yet implemented")
    }

    override fun copy(dependencySpec: Spec<in Dependency>): Configuration {
        TODO("Not yet implemented")
    }

    override fun copy(dependencySpec: Closure<*>): Configuration {
        TODO("Not yet implemented")
    }

    override fun copyRecursive(): Configuration {
        TODO("Not yet implemented")
    }

    override fun copyRecursive(dependencySpec: Spec<in Dependency>): Configuration {
        TODO("Not yet implemented")
    }

    override fun copyRecursive(dependencySpec: Closure<*>): Configuration {
        TODO("Not yet implemented")
    }

    override fun setCanBeConsumed(allowed: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isCanBeConsumed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setCanBeResolved(allowed: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setCanBeDeclared(allowed: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isCanBeDeclared(): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldResolveConsistentlyWith(versionsSource: Configuration): Configuration {
        TODO("Not yet implemented")
    }

    override fun disableConsistentResolution(): Configuration {
        TODO("Not yet implemented")
    }
}