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
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency

class FakeDependencySet(val dependencies : Set<Dependency>) : DependencySet {
    override fun iterator(): MutableIterator<Dependency> {
        return object : MutableIterator<Dependency> {
            private val innerIterator = dependencies.iterator()

            override fun hasNext(): Boolean = innerIterator.hasNext()

            override fun next(): Dependency = innerIterator.next()

            override fun remove() {
                throw UnsupportedOperationException("Remove operation is not supported for this iterator")
            }
        }
    }

    override fun contains(element: Dependency?): Boolean {
        TODO("Not yet implemented")
    }

    override fun add(element: Dependency?): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<Dependency>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }


    override fun remove(element: Dependency?): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<Dependency>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<Dependency>): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<Dependency>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun addLater(provider: Provider<out Dependency>) {
        TODO("Not yet implemented")
    }

    override fun addAllLater(provider: Provider<out MutableIterable<Dependency>>) {
        TODO("Not yet implemented")
    }

    override fun <S : Dependency?> withType(type: Class<S>): DomainObjectSet<S> {
        TODO("Not yet implemented")
    }

    override fun <S : Dependency?> withType(type: Class<S>, configureAction: Action<in S>): DomainObjectCollection<S> {
        TODO("Not yet implemented")
    }

    override fun <S : Dependency?> withType(type: Class<S>, configureClosure: Closure<*>): DomainObjectCollection<S> {
        TODO("Not yet implemented")
    }

    override fun matching(spec: Spec<in Dependency>): DomainObjectSet<Dependency> {
        TODO("Not yet implemented")
    }

    override fun matching(spec: Closure<*>): DomainObjectSet<Dependency> {
        TODO("Not yet implemented")
    }

    override fun whenObjectAdded(action: Action<in Dependency>): Action<in Dependency> {
        TODO("Not yet implemented")
    }

    override fun whenObjectAdded(action: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun whenObjectRemoved(action: Action<in Dependency>): Action<in Dependency> {
        TODO("Not yet implemented")
    }

    override fun whenObjectRemoved(action: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun all(action: Action<in Dependency>) {
        TODO("Not yet implemented")
    }

    override fun all(action: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun configureEach(action: Action<in Dependency>) {
        TODO("Not yet implemented")
    }

    override fun findAll(spec: Closure<*>): MutableSet<Dependency> {
        TODO("Not yet implemented")
    }

    override fun getBuildDependencies(): TaskDependency {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = TODO("Not yet implemented")
}