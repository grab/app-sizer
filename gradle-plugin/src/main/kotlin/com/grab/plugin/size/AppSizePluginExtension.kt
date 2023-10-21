package com.grab.plugin.size

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

open class AppSizePluginExtension(project: Project) {
    val bundleToolPath: Property<String> = project.objects.property(String::class.java)
    val bundleFilePath: Property<String> = project.objects.property(String::class.java)
    val outputDirectory: RegularFileProperty = project.objects.fileProperty()
    val featureMappingFile: RegularFileProperty = project.objects.fileProperty()
    val tag: Property<String?> = project.objects.property(String::class.java)
}