package com.grab.tools.di

import javax.inject.Qualifier


const val INPUT_LIB_DIRECTORY = "lib"
const val INPUT_ROOT_PROJECT = "root"
const val INPUT_APK_DIRECTORY = "apks"
const val INPUT_OUTPUT_FILE = "out"
const val INPUT_FEATURE_MAPPING_FILE = "mapping_file"
const val INPUT_PROGUARD_MAPPING_FILE = "proguard"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyzerInputFile(
    /** The name.  */
    val value: String = ""
)