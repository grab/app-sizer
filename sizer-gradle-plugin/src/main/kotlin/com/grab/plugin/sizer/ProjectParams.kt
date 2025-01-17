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

package com.grab.plugin.sizer

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.SizeCalculationMode
import org.gradle.api.Project

private const val DEVICE_NAME_PARAM = "deviceName"
private const val PIPELINE_ID_PARAM = "pipeline"
private const val OPTION_PARAM = "option"
private const val SIZE_PARAM = "size_mode"
private const val LIBRARY_NAME_PARAM = "library"
private const val DEVICE_SPEC_PARAM = "deviceSpec"

internal interface ProjectParams {
    fun option(): AnalyticsOption
    fun sizeMode(): SizeCalculationMode
    fun libraryName(): String?
}

internal fun Project.params(): ProjectParams = DefaultProjectParams(this)

private class DefaultProjectParams(private val project: Project) : ProjectParams {
    override fun option(): AnalyticsOption = AnalyticsOption.fromString(project.findProperty(OPTION_PARAM) as String?)
    override fun sizeMode(): SizeCalculationMode = SizeCalculationMode.fromString(project.findProperty(SIZE_PARAM) as String?)
    override fun libraryName(): String? = (project.findProperty(LIBRARY_NAME_PARAM) as String?)
}