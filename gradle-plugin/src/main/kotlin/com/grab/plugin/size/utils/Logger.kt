package com.grab.plugin.size.utils

import com.grab.tools.utils.Logger
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class PluginLogger(private val project: Project) : Logger {
    override fun log(tag: String, message: String) = project.logger.log(LogLevel.QUIET, "$tag: $message")
    override fun log(tag: String, e: Exception) = project.logger.log(LogLevel.ERROR, tag, e)
}
