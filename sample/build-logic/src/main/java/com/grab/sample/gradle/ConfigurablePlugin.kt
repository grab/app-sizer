package com.grab.sample.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ConfigurablePlugin(
    private val configuration: Project.() -> Unit
) : Plugin<Project> {
    override fun apply(project: Project): Unit = configuration(project)
}