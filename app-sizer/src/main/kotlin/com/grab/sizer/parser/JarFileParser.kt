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

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import com.grab.sizer.utils.SizerInputFile
import com.grab.sizer.SizeCalculationMode
import java.util.zip.ZipFile
import javax.inject.Inject


/**
 * JarFileParser interface provides a method to parse a sequence of JAR files into a set of [JarFileInfo].
 * Note: For native libraries (*.so), their paths will be adjusted to ensure the files reside under the "lib" folder.
 * This modification facilitates mapping to native libraries in the APK file.
 */
interface JarFileParser {
    fun parseJars(files: Sequence<SizerInputFile>): Set<JarFileInfo>
}

@AppScope
class DefaultJarFileParser @Inject constructor(
    private val sizeCalculationMode: SizeCalculationMode
) : JarFileParser {
    private fun parse(sizerInputFile: SizerInputFile): JarFileInfo {
        ZipFile(sizerInputFile.file).use { zipFile ->
            val entries = zipFile.entries()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            val classes = mutableSetOf<ClassFileInfo>()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    rawSize = entry.size,
                    downloadSize = entry.size,
                    sizeCalculationMode = sizeCalculationMode
                )
                when (fileInfo.type) {
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.CLASS -> classes.add(entry.toClass(sizeCalculationMode))
                    else -> others.add(fileInfo)
                }
            }
            return JarFileInfo(
                name = sizerInputFile.file.name,
                path = sizerInputFile.file.path,
                tag = sizerInputFile.tag,
                others = others,
                nativeLibs = nativeLibs,
                classes = classes
            )
        }
    }

    override fun parseJars(files: Sequence<SizerInputFile>): Set<JarFileInfo> {
        return files.map { file -> parse(file) }
            .toSet()
    }
}