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

package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.*
import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.*

class FakeDataPasser(
    override val apks: MutableSet<ApkFileInfo> = mutableSetOf(),
    override val libAars: MutableSet<AarFileInfo> = mutableSetOf(),
    override val libJars: MutableSet<JarFileInfo> = mutableSetOf(),
    override val moduleAars: MutableSet<AarFileInfo> = mutableSetOf(),
    override val moduleJars: MutableSet<JarFileInfo> = mutableSetOf()
) : DataParser


internal class MapperComponent {
    private val mapOfComponentMapper = mapOf(
        ResourceComponentMapper::class to ResourceComponentMapper(),
        NativeLibComponentMapper::class to NativeLibComponentMapper(),
        AssetComponentMapper::class to AssetComponentMapper(),
        ClassComponentMapper::class to ClassComponentMapper(),
        OtherComponentMapper::class to OtherComponentMapper(),
    )

    val apkComponentProcessor = DefaultApkComponentProcessor(mapOfComponentMapper.mapKeys { (k, _) -> k.java })
}


internal fun createRawFileInfo(path: String, downloadSize: Long = 50, size: Long = 150) =
    RawFileInfo(path = path, downloadSize = downloadSize, size = size)

internal fun createEmptyDexFileInfo(name: String = "dex"): DexFileInfo = DexFileInfo(
    name = name,
    downloadSize = 100,
    size = 200,
    classes = emptySet(),
)

internal fun createClassFileInfo(name: String, downloadSize: Long = 100, size: Long = 300) =
    ClassFileInfo(name = name, downloadSize = downloadSize, size = size)