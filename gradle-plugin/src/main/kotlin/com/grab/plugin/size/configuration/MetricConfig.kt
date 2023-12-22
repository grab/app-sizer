package com.grab.plugin.size.configuration

import groovy.lang.Closure
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class MetricConfig @Inject constructor(objects: ObjectFactory) {
    var influxDbPublisher: InfluxDbPublisher = objects.newInstance(InfluxDbPublisher::class.java, objects)

    val local : Local = objects.newInstance(Local::class.java, objects)

    val customAttributes: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

    fun influxDbPublisher(closure: Closure<*>) {
        closure.delegate = influxDbPublisher
        closure.call()
    }

    fun influxDbPublisher(block: InfluxDbPublisher.() -> Unit) {
        block(influxDbPublisher)
    }

    fun local(block: Local.() -> Unit) {
        block(local)
    }

    fun local(closure: Closure<*>) {
        closure.delegate = local
        closure.call()
    }
}

open class InfluxDbPublisher @Inject constructor(objects: ObjectFactory) {
    var dbName: Property<String> = objects.property(String::class.java)
    var url: Property<String> = objects.property(String::class.java)
    var username: Property<String> = objects.property(String::class.java)
    var password: Property<String> = objects.property(String::class.java)
}

open class Local @Inject constructor(objects: ObjectFactory) {
    var outputDirectory: DirectoryProperty = objects.directoryProperty()
}