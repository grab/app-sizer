package com.grab.plugin.size.utils

import org.gradle.api.Project

internal const val KOTLIN_PLUGIN = "kotlin"
internal const val ANDROID_APPLICATION_PLUGIN = "com.android.application"
internal const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
internal const val JAVA_PLUGIN = "java"

val Project.isAndroidLibrary get() = plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
val Project.isAndroidApplication get() = plugins.hasPlugin(ANDROID_APPLICATION_PLUGIN)
val Project.isKotlinJvm get() = plugins.hasPlugin(KOTLIN_PLUGIN)
val Project.isJava get() = plugins.hasPlugin(JAVA_PLUGIN)