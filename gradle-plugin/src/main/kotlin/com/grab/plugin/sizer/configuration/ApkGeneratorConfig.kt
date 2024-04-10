package com.grab.plugin.sizer.configuration

import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

open class ApkGeneratorConfig @Inject constructor(objects: ObjectFactory) {
    var bundleToolPath: Property<String> = objects.property(String::class.java)
    var deviceSpecs: ListProperty<File> = objects.listProperty(File::class.java)
}

