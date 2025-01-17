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

import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.Report
import com.grab.sizer.report.Row
import javax.inject.Inject

/**
 * A specialized implementation of the Analyzer interface that focuses on basic APK analysis.
 * This class specifically handles [com.grab.sizer.AnalyticsOption.BASIC] and provides metrics similar to those
 * obtained by opening the APK file in Android Studio, including:
 * - apk : The total download size of the app.
 * - resource : The cumulative size contribution from resources such as images and layouts.
 * - native_lib : The cumulative size contribution from native libraries.
 * - asset : The cumulative size contribution from assets.
 * - code : The cumulative size contribution from Java/Kotlin code.
 *
 * @property dataParser Parses APK, AAR and JAR for analysis.
 **/
internal class BasicApkAnalyzer @Inject constructor(
    private val dataParser: DataParser
) : Analyzer {
    override fun process(): Report {
        val androidBinaryInfo = dataParser.apks
        return Report(
            rows = androidBinaryInfo.createApkReportRows(),
            id = METRICS_ID_BASIC,
            name = METRICS_ID_BASIC,
        )
    }

    private fun Set<ApkFileInfo>.createApkReportRows(): List<Row> {
        val resourceSize = flatMap { it.resources }.sumOf { it.size }
        val nativeLibSize = flatMap { it.nativeLibs }.sumOf { it.size }
        val assetSize = flatMap { it.assets }.sumOf { it.size }
        val otherSize = flatMap { it.others }.sumOf { it.size }
        val dexSize = flatMap { it.dexes }.sumOf { it.size }
        val classSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.downloadSize }
        val total =
            resourceSize + nativeLibSize + assetSize + otherSize + classSize

        return listOf(
            createRow(
                name = "apk",
                value = total
            ),
            createRow(
                name = "resource",
                value = resourceSize
            ),
            createRow(
                name = "native_lib",
                value = nativeLibSize
            ),
            createRow(
                name = "asset",
                value = assetSize
            ),
            createRow(
                name = "other",
                value = otherSize
            ),
            createRow(
                name = "code",
                value = dexSize
            )
        )
    }
}

