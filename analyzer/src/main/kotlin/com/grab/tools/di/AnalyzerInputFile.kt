package com.grab.tools.di

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.inject.Qualifier


const val INPUT_FILE_LIB_DIRECTORY = "lib"
const val INPUT_FILE_ROOT_PROJECT = "root"
const val INPUT_FILE_APK_DIRECTORY = "apks"
const val INPUT_FILE_OUTPUT_FILE = "out"
const val INPUT_FILE_FEATURE_MAPPING_FILE = "mapping_file"
const val INPUT_FILE_PROGUARD_MAPPING_FILE = "proguard"

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
annotation class AnalyzerInputFile(
    /** The name.  */
    val value: String = ""
)