package com.grab.plugin.size.utils

import org.gradle.api.Project

const val KOTLIN_PLUGIN = "kotlin"
const val ANDROID_APPLICATION_PLUGIN = "com.android.application"
const val ANDROID_LIBRARY_PLUGIN = "com.android.library"

val Project.isAndroidLibrary get() = plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
val Project.isAndroidApplication get() = plugins.hasPlugin(ANDROID_APPLICATION_PLUGIN)
val Project.isKotlinJvm get() = plugins.hasPlugin(KOTLIN_PLUGIN)