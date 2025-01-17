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

package com.grab.sizer.report

import com.grab.sizer.analyzer.ReportItem
import com.grab.sizer.parser.ApkFileInfo

internal fun Set<ApkFileInfo>.apksSizeReport(): ReportItem {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val classDownloadSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.downloadSize }
    val dexDownloadSize = flatMap { it.dexes }.sumOf { it.downloadSize }
    val total =
        resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + dexDownloadSize

    return ReportItem(
        id = "apk",
        totalDownloadSize = total,
        name = "Apks",
        resourceDownloadSize = resourceDownloadSize,
        nativeLibDownloadSize = nativeLibDownloadSize,
        assetDownloadSize = assetDownloadSize,
        otherDownloadSize = otherDownloadSize,
        classesDownloadSize = classDownloadSize,
        extraInfo = "Apk breakdown by component sizer"
    )
}

internal fun Set<ApkFileInfo>.toReportField(): List<Field> {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val dexDownloadSize = flatMap { it.dexes }.sumOf { it.downloadSize }
    val total =
        resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + dexDownloadSize
    return listOf(
        TagField(
            name = FIELD_KEY_CONTRIBUTOR,
            value = "apk"
        ),
        DefaultField(
            name = FIELD_KEY_SIZE,
            value = total
        )
    )
}