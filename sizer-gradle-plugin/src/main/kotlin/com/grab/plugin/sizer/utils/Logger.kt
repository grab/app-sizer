/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.plugin.sizer.utils

import com.grab.plugin.sizer.dependencies.DependenciesScope
import com.grab.sizer.utils.DEFAULT_TAG
import com.grab.sizer.utils.Logger
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import javax.inject.Inject


interface PluginLogger : Logger {
    fun warn(tag: String, message: String)
    fun warn(tag: String, message: String, e: Exception)

    fun debug(tag: String, message: String)
    fun debug(tag: String, message: String, e: Exception)
}

fun PluginLogger.warn(message: String) {
    warn(DEFAULT_TAG, message)
}

fun PluginLogger.warn(message: String, e: Exception) {
    warn(DEFAULT_TAG, message, e)
}

fun PluginLogger.debug(message: String) {
    debug(DEFAULT_TAG, message)
}

fun PluginLogger.debug(message: String, e: Exception) {
    debug(DEFAULT_TAG, message, e)
}


@DependenciesScope
class DefaultPluginLogger @Inject constructor(private val project: Project) : PluginLogger {
    override fun log(tag: String, message: String) = project.logger.log(LogLevel.QUIET, "$tag: $message")
    override fun log(tag: String, message: String, e: Exception) =
        project.logger.log(LogLevel.DEBUG, "$tag: $message", e)

    override fun warn(tag: String, message: String) = project.logger.warn("$tag: $message")

    override fun warn(tag: String, message: String, e: Exception) = project.logger.warn("$tag: $message", e)

    override fun debug(tag: String, message: String) = project.logger.debug("$tag: $message")
    override fun debug(tag: String, message: String, e: Exception) = project.logger.debug("$tag: $message", e)
}
