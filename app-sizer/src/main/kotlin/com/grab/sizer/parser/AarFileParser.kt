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

import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import com.grab.sizer.utils.SizerInputFile
import com.grab.sizer.SizeCalculationMode
import java.util.zip.ZipFile
import javax.inject.Inject

/**
 * The AarFileParser interface provides the method to parse a sequence of AAR files into a set of [AarFileInfo].
 * Note: For native libraries located in the "jni" folder, their paths will be converted with "jni" replaced by "lib".
 * This adjustment ensures the path inside the AAR file matches the path in the APK file.
 */
interface AarFileParser {
    fun parseAars(files: Sequence<SizerInputFile>): Set<AarFileInfo>
}

/**
 * Default implementation of [AarFileParser].
 * For more about the AAR file format, see: http://tools.android.com/tech-docs/new-build-system/aar-format
 */
@AppScope
class DefaultAarFileParser @Inject constructor(
    private val jarParser: JarStreamParser,
    private val sizeCalculationMode: SizeCalculationMode
) : AarFileParser {

    private fun parse(sizerInputFile: SizerInputFile): AarFileInfo {
        ZipFile(sizerInputFile.file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            val jars = mutableSetOf<JarFileInfo>()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    rawSize = entry.size,
                    downloadSize = entry.size,
                    sizeCalculationMode = sizeCalculationMode
                )

                when (fileInfo.type) {
                    FileType.RESOURCE -> resources.add(fileInfo)
                    FileType.ASSET -> assets.add(fileInfo)
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.JAR -> {
                        jars.add(jarParser.parse(entry, zipFile.getInputStream(entry)))
                    }

                    else -> others.add(fileInfo)
                }
            }
            return AarFileInfo(
                name = sizerInputFile.file.name,
                path = sizerInputFile.file.path,
                tag = sizerInputFile.tag,
                resources = resources,
                assets = assets,
                nativeLibs = nativeLibs,
                others = others,
                jars = jars
            )
        }
    }

    override fun parseAars(files: Sequence<SizerInputFile>): Set<AarFileInfo> {
        return files.map { file -> parse(file) }.toSet()
    }
}


