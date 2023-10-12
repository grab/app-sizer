package com.grab.plugin.size

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

open class AppSizePluginExtension(project: Project) {
    val deviceConfig: Property<String> = project.objects.property(String::class.java)
    val bundleToolPath: Property<String> = project.objects.property(String::class.java)
    val bundleFilePath: Property<String> = project.objects.property(String::class.java)
    val tag: Property<String> = project.objects.property(String::class.java)
    val outputFolder: RegularFileProperty = project.objects.fileProperty()
    val featureMappingFile: RegularFileProperty = project.objects.fileProperty()
}