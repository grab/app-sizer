package com.grab.plugin.sizer.configuration

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import java.io.File
import javax.inject.Inject

open class ApkGeneratorExtension @Inject constructor(objects: ObjectFactory) {
    var bundleToolFile: RegularFileProperty = objects.fileProperty()
    var deviceSpecs: ListProperty<File> = objects.listProperty(File::class.java)
}

