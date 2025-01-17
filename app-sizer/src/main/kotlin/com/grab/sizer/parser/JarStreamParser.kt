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

import com.grab.sizer.SizeCalculationMode
import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject


/**
 * JarStreamParser interface provides a method to parse a JAR file within an AAR.
 * It uses the ZipEntry of the JAR file and a provided InputStream to access JAR content within the AAR file.
 */
interface JarStreamParser {
    /**
     * Parses the contents of a JAR file within the AAR file.
     * @param jarEntry ZipEntry of the JAR file within the AAR file.
     * @param inputStream InputStream to access the JAR content.
     * @return A JarFileInfo object containing the properties of parsed JAR file.
     */
    fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo
}

@AppScope
class DefaultJarStreamParser @Inject constructor(
    private val sizeCalculationMode: SizeCalculationMode
) : JarStreamParser {
    override fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo {
        ZipInputStream(inputStream).use { entries ->
            val others = mutableSetOf<RawFileInfo>()
            val classes = mutableSetOf<ClassFileInfo>()
            var entry = entries.nextEntry
            while (entry != null) {
                val fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    rawSize = entry.size,
                    downloadSize = entry.size,
                    sizeCalculationMode = sizeCalculationMode
                )
                when (fileInfo.type) {
                    FileType.CLASS -> classes.add(entry.toClass(sizeCalculationMode))
                    else -> others.add(fileInfo)
                }
                entry = entries.nextEntry
            }
            return JarFileInfo(
                name = jarEntry.getPath(),
                path = "",
                tag = "",
                others = others,
                nativeLibs = emptySet(),
                classes = classes
            )
        }
    }
}

internal fun ZipEntry.toClass(sizeCalculationMode: SizeCalculationMode): ClassFileInfo {
    return ClassFileInfo(
        /**
         * Convert ZipEntry name to class name
         * Example: "/com/grab/sample/dummy/DummyClass1.class" -> "com.grab.sample.dummy.DummyClass1"
         */
        name = name.replace('/', '.').removeSuffix(".class"),
        rawSize = size,
        downloadSize = size,
        sizeCalculationMode = sizeCalculationMode
    )
}


