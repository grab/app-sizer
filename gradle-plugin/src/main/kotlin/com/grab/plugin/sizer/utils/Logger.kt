package com.grab.plugin.sizer.utils

import com.grab.plugin.sizer.dependencies.DependenciesScope
import com.grab.sizer.utils.Logger
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import javax.inject.Inject

@DependenciesScope
class PluginLogger @Inject constructor(private val project: Project) : Logger {
    override fun log(tag: String, message: String) = project.logger.log(LogLevel.QUIET, "$tag: $message")

    override fun logDebug(tag: String, message: String) = project.logger.log(LogLevel.DEBUG, "$tag: $message")
    override fun log(tag: String, e: Exception) = project.logger.log(LogLevel.DEBUG, tag, e)
}
