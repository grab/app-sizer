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

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import com.grab.sizer.SizeCalculationMode
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject


internal interface ApkFileParser {
    /**
     * Parses a sequence of APK files and use R8 mapping file to extract and return the set of APK file information.
     * This method de-obfuscates class names to make them readable, and estimates the download size of each file
     * in the [ApkFileInfo] output.
     *
     * @param apks A sequence of APK files to be parsed.
     * @param proguardMap A ProguardMap used for de-obfuscating class names in the APK files.
     * @return A set of ApkFileInfo instances, each representing information about a parsed APK file.
     */
    fun parseApks(apks: Sequence<File>, proguardMap: ProguardMap): Set<ApkFileInfo>
}

@AppScope
internal class DefaultApkFileParser @Inject constructor(
    private val dexFileParser: DexFileParser,
    private val apkSizeCalculator: ApkSizeCalculator,
    private val sizeCalculationMode: SizeCalculationMode
) : ApkFileParser {
    override fun parseApks(apks: Sequence<File>, proguardMap: ProguardMap): Set<ApkFileInfo> = apks
        .map { apkFile -> parse(apkFile, proguardMap) }
        .toSet()

    private fun parse(file: File, proguardMap: ProguardMap): ApkFileInfo {
        val apkSizeInfo = apkSizeCalculator.parseSize(file.toPath())
        return parseApkFile(file, apkSizeInfo, proguardMap)
    }

    private fun parseApkFile(file: File, apkSizeInfo: ApkSizeInfo, proguardMap: ProguardMap): ApkFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            val dexes = mutableSetOf<DexFileInfo>()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val path = entry.getPath()
                val downloadSize = apkSizeInfo.downloadFileSizeMap[path] ?: 0
                val rawSize = apkSizeInfo.rawFileSizeMap[path] ?: 0

                val fileInfo = RawFileInfo(
                    path = path,
                    downloadSize = downloadSize,
                    rawSize = rawSize,
                    sizeCalculationMode = sizeCalculationMode
                )

                when (fileInfo.type) {
                    FileType.RESOURCE -> resources.add(fileInfo)
                    FileType.ASSET -> assets.add(fileInfo)
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.DEX -> dexes.add(
                        dexFileParser.parse(
                            entry,
                            zipFile.getInputStream(entry),
                            apkSizeInfo,
                            proguardMap
                        )
                    )

                    else -> others.add(fileInfo)
                }
            }

            return ApkFileInfo(
                name = file.name,
                resources = resources,
                assets = assets,
                nativeLibs = nativeLibs,
                others = others,
                dexes = dexes,
            )
        }
    }

    private fun ApkSizeCalculator.parseSize(path: Path): ApkSizeInfo = ApkSizeInfo(
        downloadSize = getFullApkDownloadSize(path),
        size = getFullApkRawSize(path),
        downloadFileSizeMap = getDownloadSizePerFile(path),
        rawFileSizeMap = getRawSizePerFile(path)
    )
}

internal fun ZipEntry.getPath() = "/$name"

internal class ApkSizeInfo(
    val downloadSize: Long,
    val size: Long,
    val downloadFileSizeMap: Map<String, Long>,
    val rawFileSizeMap: Map<String, Long>
)