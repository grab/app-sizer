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
import javax.inject.Inject

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on assets.
 */
internal class AssetComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        val apkAssets = flatMap { apk -> apk.assets }

        val aarsAssetMap = mutableMapOf<RawFileInfo, BinaryFileInfo>().apply {
            aars.forEach { aar ->
                aar.assets.forEach { file ->
                    put(file, aar)
                }
            }
        }
        val noOwnerAssets = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<BinaryFileInfo, MutableSet<RawFileInfo>>().apply {
            apkAssets.forEach { asset ->
                val aar = aarsAssetMap[asset]
                if (aar != null) {
                    putIfAbsent(aar, mutableSetOf())
                    get(aar)?.add(asset)
                } else {
                    noOwnerAssets.add(asset)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerAssets
        )
    }
}