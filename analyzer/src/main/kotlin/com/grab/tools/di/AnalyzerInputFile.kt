package com.grab.tools.di

import javax.inject.Qualifier


const val INPUT_FILE_LIB_DIRECTORY = "lib"
const val INPUT_FILE_ROOT_PROJECT = "root"
const val INPUT_FILE_APK_DIRECTORY = "apks"
const val INPUT_FILE_OUTPUT_FILE = "out"
const val INPUT_FILE_FEATURE_MAPPING_FILE = "mapping_file"
const val INPUT_FILE_PROGUARD_MAPPING_FILE = "proguard"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyzerInputFile(
    /** The name.  */
    val value: String = ""
)