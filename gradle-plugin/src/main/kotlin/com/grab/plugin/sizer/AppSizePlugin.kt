package com.grab.plugin.sizer

import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val PLUGIN_EXTENSION = "appSizeAnalysis"

class AppSizePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        TaskManager(
            project,
            project.extensions.create(PLUGIN_EXTENSION, AppSizePluginExtension::class.java)
        ).configTasks()
    }
}
