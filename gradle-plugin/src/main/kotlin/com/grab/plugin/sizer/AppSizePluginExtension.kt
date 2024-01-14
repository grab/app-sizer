package com.grab.plugin.sizer

import com.grab.plugin.sizer.configuration.ApkGeneratorConfig
import com.grab.plugin.sizer.configuration.MetricConfig
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty


open class AppSizePluginExtension(val project: Project) {
    var apk: ApkGeneratorConfig = project.objects.newInstance(ApkGeneratorConfig::class.java, project.objects)
    var metrics = project.objects.newInstance(MetricConfig::class.java, project.objects)
    var featureMappingFile: RegularFileProperty = project.objects.fileProperty()

    fun metrics(block: MetricConfig.() -> Unit) {
        block(metrics)
    }

    fun metrics(action: Action<in MetricConfig>) {
        action.execute(metrics)
    }

    fun apk(action: Action<in ApkGeneratorConfig>) {
        action.execute(apk)
    }

    fun apk(block: ApkGeneratorConfig.() -> Unit) {
        block(apk)
    }
}


