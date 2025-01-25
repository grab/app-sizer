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

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

internal const val ANDROID_APPLICATION_PLUGIN = "com.android.application"
internal const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
internal const val JAVA_LIB_PLUGIN = "java-library"
internal const val KOTLIN_LIB_PLUGIN = "org.jetbrains.kotlin.jvm"
internal const val KOTLIN_MPP_PLUGIN = "org.jetbrains.kotlin.multiplatform"

val Project.isAndroidLibrary get() = plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
val Project.isAndroidApplication get() = plugins.hasPlugin(ANDROID_APPLICATION_PLUGIN)
val Project.isKotlinJvm get() = plugins.hasPlugin(KOTLIN_LIB_PLUGIN)
val Project.isJava get() = plugins.hasPlugin(JAVA_LIB_PLUGIN) || plugins.hasPlugin(JavaPlugin::class.java)
val Project.isKotlinMultiplatform get() = plugins.hasPlugin(KOTLIN_MPP_PLUGIN)