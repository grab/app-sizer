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

package com.grab.plugin.sizer.configuration

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class MetricExtension @Inject constructor(project: Project) {
    val influxDBExtension: InfluxDBExtension =
        project.objects.newInstance(InfluxDBExtension::class.java, project.objects)

    val localExtension: LocalExtension = project.objects.newInstance(LocalExtension::class.java, project)

    val customAttributes: MapProperty<String, String> =
        project.objects.mapProperty(String::class.java, String::class.java)

    fun influxDB(closure: Closure<*>) {
        closure.delegate = influxDBExtension
        closure.call()
    }

    fun influxDB(block: InfluxDBExtension.() -> Unit) {
        block(influxDBExtension)
    }

    fun local(block: LocalExtension.() -> Unit) {
        block(localExtension)
    }

    fun local(closure: Closure<*>) {
        closure.delegate = localExtension
        closure.call()
    }
}

open class RetentionPolicyExtension @Inject constructor(objects: ObjectFactory) {
    val name: Property<String> = objects.property<String>()
    val duration: Property<String> = objects.property<String>()
    val shardDuration: Property<String> = objects.property<String>()
    val replicationFactor: Property<Int> = objects.property<Int>()
    val setAsDefault: Property<Boolean> = objects.property<Boolean>().convention(false)
}

open class InfluxDBExtension @Inject constructor(private val objects: ObjectFactory) {
    val dbName: Property<String> = objects.property<String>()
    val url: Property<String> = objects.property<String>()
    val username: Property<String> = objects.property<String>()
    val password: Property<String> = objects.property<String>()
    val reportTableName: Property<String> = objects.property<String>()
    val retentionPolicy: RetentionPolicyExtension = objects.newInstance(RetentionPolicyExtension::class.java)

    fun retentionPolicy(closure: Closure<*>) {
        closure.delegate = retentionPolicy
        closure.call()
    }

    fun retentionPolicy(block: RetentionPolicyExtension.() -> Unit) {
        retentionPolicy.block()
    }
}


open class LocalExtension @Inject constructor(project: Project) {
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
}