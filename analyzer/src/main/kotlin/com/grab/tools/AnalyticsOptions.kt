package com.grab.tools

enum class AnalyticsOption {
    LIBRARIES_ANALYTICS,
    APK_ANALYTICS,
    BASIC_APK_ANALYTICS,
    MODULE_ANALYTICS,
    GENERAL,
    LARGE_FILE,
    LIB_CONTENT;

    companion object {
        fun fromString(value: String): AnalyticsOption = when (value) {
            "libraries" -> AnalyticsOption.LIBRARIES_ANALYTICS
            "modules" -> AnalyticsOption.MODULE_ANALYTICS
            "apk" -> AnalyticsOption.APK_ANALYTICS
            "basic" -> AnalyticsOption.BASIC_APK_ANALYTICS
            "general" -> AnalyticsOption.GENERAL
            "large-files" -> AnalyticsOption.LARGE_FILE
            "lib-content" -> AnalyticsOption.LIB_CONTENT
            else -> AnalyticsOption.GENERAL
        }
    }
}

