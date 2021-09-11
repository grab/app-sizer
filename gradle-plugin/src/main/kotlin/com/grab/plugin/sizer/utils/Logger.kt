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
