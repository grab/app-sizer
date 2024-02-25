package com.grab.plugin.sizer

import com.grab.plugin.sizer.configuration.AndroidExtension
import com.grab.plugin.sizer.configuration.MetricConfig
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty


open class AppSizePluginExtension(val project: Project) {
    var android = project.objects.newInstance(AndroidExtension::class.java, project.objects)
    var metrics = project.objects.newInstance(MetricConfig::class.java, project.objects)
    var featureMappingFile: RegularFileProperty = project.objects.fileProperty()

    fun android(action: Action<in AndroidExtension>) {
        action.execute(android)
    }

    fun android(block: AndroidExtension.() -> Unit) {
        block(android)
    }
    fun metrics(block: MetricConfig.() -> Unit) {
        block(metrics)
    }

    fun metrics(action: Action<in MetricConfig>) {
        action.execute(metrics)
    }

}


