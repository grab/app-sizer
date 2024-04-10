package com.grab.plugin.sizer

import com.grab.plugin.sizer.configuration.AndroidExtension
import com.grab.plugin.sizer.configuration.MetricExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty


open class AppSizePluginExtension(val project: Project) {
    var enabled = true
    var android = project.objects.newInstance(AndroidExtension::class.java, project.objects)
    var metrics = project.objects.newInstance(MetricExtension::class.java, project.objects)
    var featureMappingFile: RegularFileProperty = project.objects.fileProperty()

    fun android(action: Action<in AndroidExtension>) {
        action.execute(android)
    }

    fun android(block: AndroidExtension.() -> Unit) {
        block(android)
    }
    fun metrics(block: MetricExtension.() -> Unit) {
        block(metrics)
    }

    fun metrics(action: Action<in MetricExtension>) {
        action.execute(metrics)
    }

}


