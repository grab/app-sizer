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

package com.grab.sizer.parser

import com.grab.sizer.di.AppScope
import com.grab.sizer.utils.InputProvider
import javax.inject.Inject


/**
 * The DataParser interface is used to parse all binary files, including AAR, JAR and APK files.
 * It acts as a provider for all binaries that the app-sizer needs for analysis and output reports.
 * It delivers a set of parsed APKs, all parsed AAR & JAR libraries, and all parsed AAR & JAR modules.
 */
internal interface DataParser {
    val apks: Set<ApkFileInfo>
    val libAars: Set<AarFileInfo>
    val libJars: Set<JarFileInfo>
    val moduleAars: Set<AarFileInfo>
    val moduleJars: Set<JarFileInfo>
}

/**
 * Combine all AAR library and module files into one set.
 */
internal fun DataParser.getAars() = moduleAars + libAars

/**
 * Combine all JAR library and module files into one set.
 */
internal fun DataParser.getJars() = libJars + moduleJars

/**
 * This default implementation of [DataParser] will cache all the binaries content after parsed.
 * The parsed values will be reused by different [com.grab.sizer.analyzer.Analyzer]
 */
@AppScope
internal class DefaultDataParser @Inject constructor(
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val inputProvider: InputProvider,
    private val proguardParser: ProguardFileParser
) : DataParser {
    override val apks: Set<ApkFileInfo> by lazy {
        apkFileParser.parseApks(
            inputProvider.provideApkFiles(),
            proguardParser.parse(inputProvider.provideR8MappingFile())
        )
    }
    override val libAars: Set<AarFileInfo> by lazy {
        aarFileParser.parseAars(inputProvider.provideLibraryAar())
    }
    override val libJars: Set<JarFileInfo> by lazy {
        jarFileParser.parseJars(inputProvider.provideLibraryJar())
    }
    override val moduleAars: Set<AarFileInfo> by lazy {
        aarFileParser.parseAars(inputProvider.provideModuleAar())
    }
    override val moduleJars: Set<JarFileInfo> by lazy {
        jarFileParser.parseJars(inputProvider.provideModuleJar())
    }
}