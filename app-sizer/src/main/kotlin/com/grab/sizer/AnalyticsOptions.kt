package com.grab.sizer

import java.io.Serializable

enum class AnalyticsOption : Serializable{
    LIBRARIES,
    DEFAULT,
    APK,
    BASIC,
    MODULES,
    CODEBASE,
    LARGE_FILE,
    LIB_CONTENT;

    companion object {
        fun fromString(value: String?): AnalyticsOption = when (value) {
            "libraries" -> LIBRARIES
            "modules" -> MODULES
            "apk" -> APK
            "basic" -> BASIC
            "codebase" -> CODEBASE
            "large-files" -> LARGE_FILE
            "lib-content" -> LIB_CONTENT
            else -> DEFAULT
        }
    }
}

