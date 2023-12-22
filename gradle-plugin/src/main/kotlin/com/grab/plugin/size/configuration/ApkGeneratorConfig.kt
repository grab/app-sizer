package com.grab.plugin.size.configuration

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class ApkGeneratorConfig @Inject constructor(objects: ObjectFactory) {
    var bundleToolPath: Property<String> = objects.property(String::class.java)
    var bundleFilePath: Property<String> = objects.property(String::class.java)
}

