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

package com.grab.sizer.analyzer.model

import com.grab.sizer.parser.BinaryFileInfo

/**
 * Represents a aar or a jar file, and their components (assets, resources, native libraries, classes, and others).
 * These component are files and classes, each component should provide the sizes it contributes to the apk.
 *
 * @property originalOwner aar/jar file
 * @property assets a set of assets files
 * @property resources a set of resources files
 * @property nativeLibs a set of native libraries files (*.so files)
 * @property classes a set of classes
 * @property others a set of other files not categorized as assets, resources, native libraries or classes.
 */
data class Contributor(
    val originalOwner: BinaryFileInfo,
    val assets: Set<RawFileInfo> = emptySet(),
    val resources: Set<RawFileInfo> = emptySet(),
    val nativeLibs: Set<RawFileInfo> = emptySet(),
    val classes: Set<ClassFileInfo> = emptySet(),
    val others: Set<RawFileInfo> = emptySet(),
) {
    val tag : String
        get() = originalOwner.tag
    val path: String
        get() = originalOwner.path
    // Calculates the sum of the download sizes of all resources
    val resourcesDownloadSize: Long by lazy { resources.sumOf { resource -> resource.downloadSize } }

    // Calculates the sum of the download sizes of all native libraries
    val nativeLibDownloadSize: Long by lazy { nativeLibs.sumOf { lib -> lib.downloadSize } }

    // Calculates the sum of the download sizes of all assets
    val assetsDownloadSize: Long by lazy { assets.sumOf { asset -> asset.downloadSize } }

    // Calculates the sum of the download sizes of all "other" files
    val othersDownloadSize: Long by lazy { others.sumOf { other -> other.downloadSize } }

    // Calculates the sum of the sizes of all classes
    val classDownloadSize: Long by lazy { classes.sumOf { clazz -> clazz.downloadSize } }

    // Calculates the total downloadable size of all component types (assets, resources, native libraries, classes, others).
    fun getDownloadSize(): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + classDownloadSize

    override fun equals(other: Any?): Boolean {
        if (other is Contributor) {
            return path == other.path
        }

        return super.equals(other)
    }

    override fun hashCode(): Int = path.hashCode()
}