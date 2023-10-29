package com.grab.tools

enum class AnalyticsOption {
    LIBRARIES,
    APK,
    BASIC,
    MODULES,
    CODEBASE,
    LARGE_FILE,
    LIB_CONTENT;

    companion object {
        fun fromString(value: String): AnalyticsOption? = when (value) {
            "libraries" -> LIBRARIES
            "modules" -> MODULES
            "apk" -> APK
            "basic" -> BASIC
            "codebase" -> CODEBASE
            "large-files" -> LARGE_FILE
            "lib-content" -> LIB_CONTENT
            else -> null
        }
    }
}

