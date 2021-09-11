/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

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
    fun provideModuleAar(): Sequence<SizerInputFile>
    fun provideModuleJar(): Sequence<SizerInputFile>
    fun provideLibraryJar(): Sequence<SizerInputFile>
    fun provideLibraryAar(): Sequence<SizerInputFile>
    fun provideApkFiles(): Sequence<File>
    fun provideR8MappingFile(): File?
    fun provideTeamMappingFile(): File?
    fun provideLargeFileThreshold(): Long
}

data class SizerInputFile(
    val file: File,
    val tag: String,
)

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

