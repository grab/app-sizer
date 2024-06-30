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