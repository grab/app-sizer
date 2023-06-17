package com.grab.plugin.size.utils

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
const val TAG = "AppSize"
inline fun Project.log(msg: Any) {
    logger.log(LogLevel.QUIET, "$TAG $msg")
}

inline fun  Project.logError(msg: Any) {
    logger.log(LogLevel.ERROR, "$TAG $msg")
}