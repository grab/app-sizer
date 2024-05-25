package com.grab.sizer

import java.io.Serializable

/**
 * The AnalyticsOption enum represents reporting options for the app-sizer tool:
 * - LIBRARIES: Generates a report on library metrics, providing insights into how each library contributes to the total app download size.
 * - DEFAULT: Generates a comprehensive reports covering all options except LIB_CONTENT.
 * - APK: Generates a report breaking down the App Download Size by Components. Sections include android-java-libraries, codebase-kotlin-java, codebase-resources, codebase-assets, codebase-native, and native libraries.
 * - BASIC: Provides a fundamental breakdown report of the App Download size, similar to opening the APK in Android Studio.
 * - MODULES: Generates a report showing the contribution of each module to the total App Download Size. Grouping by team should be feasible.
 * - CODEBASE: Produces a report showing the contribution of each team to the total App Download Size.
 * - LARGE_FILE: Generates a list of files whose sizes exceed a certain threshold.
 * - LIB_CONTENT: Provides a breakdown of a single library's size contribution (Resources, Assets, Native libraries, Classes, others).
 */
enum class AnalyticsOption : Serializable {
    LIBRARIES,
    DEFAULT,
    APK,
    BASIC,
    MODULES,
    CODEBASE,
    LARGE_FILE,
    LIB_CONTENT;

    companion object {
        /**
         * Converts a string value to the corresponding AnalyticsOption.
         * If the value does not match any predefined options, the DEFAULT option is chosen.
         */
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

