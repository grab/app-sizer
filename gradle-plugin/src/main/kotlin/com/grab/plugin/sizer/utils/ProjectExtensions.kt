package com.grab.plugin.sizer.utils

import org.gradle.api.Project

internal const val ANDROID_APPLICATION_PLUGIN = "com.android.application"
internal const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
internal const val JAVA_LIB_PLUGIN = "java-library"
internal const val KOTLIN_LIB_PLUGIN = "org.jetbrains.kotlin.jvm"

val Project.isAndroidLibrary get() = plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
val Project.isAndroidApplication get() = plugins.hasPlugin(ANDROID_APPLICATION_PLUGIN)
val Project.isKotlinJvm get() = plugins.hasPlugin(KOTLIN_LIB_PLUGIN)
val Project.isJava get() = plugins.hasPlugin(JAVA_LIB_PLUGIN)