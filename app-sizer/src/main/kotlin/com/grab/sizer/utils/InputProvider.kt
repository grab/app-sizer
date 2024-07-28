package com.grab.sizer.utils

import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.db.InfluxDBConfig
import java.io.File


/**
 * The InputProvider interface is used to provide all necessary inputs for the app-sizer tool to process.
 * The client of the app-sizer should provide these information details for the tool to process.
 * Currently, the interface is implemented in two modules: the command-line tool (cli) and the Gradle plugin.
 */
interface InputProvider {
    fun provideModuleAar(): Sequence<File>
    fun provideModuleJar(): Sequence<File>
    fun provideLibraryJar(): Sequence<File>
    fun provideLibraryAar(): Sequence<File>
    fun provideApkFiles(): Sequence<File>
    fun provideR8MappingFile(): File?
    fun provideTeamMappingFile(): File?
    fun provideLargeFileThreshold(): Long
}

/**
 * The OutputProvider interface is responsible for providing all output configurations
 * for the app-sizer tool to correctly export its output.
 * The client of the app-sizer should provide these configuration details for the tool to process.
 * Like the InputProvider, this interface is currently implemented by the command-line tool (cli) and the Gradle plugin.
 */
interface OutputProvider {
    fun provideInfluxDbConfig(): InfluxDBConfig?
    fun provideOutPutDirectory(): File

    fun provideProjectInfo(): ProjectInfo
    fun provideCustomProperties(): CustomProperties
}