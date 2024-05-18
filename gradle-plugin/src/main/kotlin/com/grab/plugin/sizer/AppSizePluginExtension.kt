package com.grab.plugin.sizer

import com.grab.plugin.sizer.configuration.InputExtension
import com.grab.plugin.sizer.configuration.MetricExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty


open class AppSizePluginExtension(val project: Project) {
    var enabled = true
    var input = project.objects.newInstance(InputExtension::class.java, project.objects)
    var metrics = project.objects.newInstance(MetricExtension::class.java, project)

    fun projectInput(action: Action<in InputExtension>) {
        action.execute(input)
    }

    fun projectInput(block: InputExtension.() -> Unit) {
        block(input)
    }
    fun metrics(block: MetricExtension.() -> Unit) {
        block(metrics)
    }

    fun metrics(action: Action<in MetricExtension>) {
        action.execute(metrics)
    }

}


