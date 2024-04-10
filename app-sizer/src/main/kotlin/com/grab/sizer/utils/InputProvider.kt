package com.grab.sizer.utils

import com.grab.sizer.report.db.InfluxDBConfig
import java.io.File

interface InputProvider {
    fun provideModuleAar(): Sequence<File>
    fun provideModuleJar(): Sequence<File>
    fun provideLibraryJar(): Sequence<File>
    fun provideLibraryAar(): Sequence<File>
    fun provideApkFiles(): Sequence<File>
    fun provideR8MappingFile(): File?
    fun provideFeatureMappingFile(): File?
}

interface OutputProvider {
    fun provideInfluxDbConfig(): InfluxDBConfig?
    fun provideOutPutDirectory(): File
}