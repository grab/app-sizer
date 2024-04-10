package com.grab.plugin.sizer.configuration

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class MetricExtension @Inject constructor(project: Project) {
    var influxDBExtension: InfluxDBExtension =
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
    var name: Property<String> = objects.property(String::class.java).convention("app_sizer")
    var duration: Property<String> = objects.property(String::class.java).convention("360d")
    var shardDuration: Property<String> = objects.property(String::class.java).convention("0m")
    var replicationFactor: Property<Int> = objects.property(Int::class.java).convention(2)
    var isDefault: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
}

open class InfluxDBExtension @Inject constructor(objects: ObjectFactory) {
    var dbName: Property<String> = objects.property(String::class.java)
    var url: Property<String> = objects.property(String::class.java)
    var username: Property<String> = objects.property(String::class.java)
    var password: Property<String> = objects.property(String::class.java)
    var retentionPolicy: RetentionPolicyExtension = objects.newInstance(RetentionPolicyExtension::class.java, objects)

    fun retentionPolicy(closure: Closure<*>) {
        closure.delegate = retentionPolicy
        closure.call()
    }

    fun retentionPolicy(block: RetentionPolicyExtension.() -> Unit) {
        block(retentionPolicy)
    }
}


open class LocalExtension @Inject constructor(project: Project) {
    var outputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
        project.layout.buildDirectory.dir("sizer/reports")
    )
}