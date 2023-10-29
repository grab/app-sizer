package com.grab.tools

enum class AnalyticsOption {
    LIBRARIES,
    APK,
    BASIC_APK,
    MODULES,
    CODEBASE,
    LARGE_FILE,
    LIB_CONTENT;

    companion object {
        fun fromString(value: String): AnalyticsOption? = when (value) {
            "libraries" -> AnalyticsOption.LIBRARIES
            "modules" -> AnalyticsOption.MODULES
            "apk" -> AnalyticsOption.APK
            "basic" -> AnalyticsOption.BASIC_APK
            "codebase" -> AnalyticsOption.CODEBASE
            "large-files" -> AnalyticsOption.LARGE_FILE
            "lib-content" -> AnalyticsOption.LIB_CONTENT
            else -> null
        }
    }
}

