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

package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo
import java.io.File
import javax.inject.Inject

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on native libraries.
 */
internal class NativeLibComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentMapperResult {
        val apkLibs = flatMap { apk -> apk.nativeLibs }

        val libraryMap = mutableMapOf<RawFileInfo, BinaryFileInfo>().apply {
            aars.forEach { aar ->
                aar.nativeLibs.forEach { file ->
                    put(file.trimPath(), aar)
                }
            }
            jars.forEach { jar ->
                jar.nativeLibs.forEach { file ->
                    put(file.trimPath(), jar)
                }
            }
        }
        val noOwnerNativeLib = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<BinaryFileInfo, MutableSet<RawFileInfo>>().apply {
            apkLibs.forEach { nativeLib ->
                val lib = libraryMap[nativeLib.trimPath()]
                if (lib != null) {
                    putIfAbsent(lib, mutableSetOf())
                    get(lib)?.add(nativeLib)
                } else {
                    noOwnerNativeLib.add(nativeLib)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerNativeLib
        )
    }

    /**
     * There are different between APK and AAR native file path.
     * This method will remove the pre-fix path for the so file, to ensure the mapping working as expected
     * Example,
     * APK: /lib/armeabi-v7a/sample.so -> armeabi-v7a/sample.so
     * AAR: /jni/armeabi-v7a/sample.so -> armeabi-v7a/sample.so
     */
    private fun RawFileInfo.trimPath(): RawFileInfo {
        val file = File(path)
        val parent = File(path).parentFile.name
        return copy(
            path = "/$parent/${file.name}"
        )
    }
}