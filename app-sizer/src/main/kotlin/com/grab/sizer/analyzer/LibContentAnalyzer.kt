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

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.di.NAMED_LIB_NAME
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.Report
import com.grab.sizer.report.Row
import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * A specific implementation of the Analyzer interface with a focus on analysis a library content.
 * This class handles [com.grab.sizer.AnalyticsOption.LIB_CONTENT] and generates a detail report on the library content
 *
 * @property apkComponentProcessor Responsible for processing APK, AAR, or JAR files to compile a list of contributors.
 * @property dataParser Parse APK, AAR, or JAR files.
 */
internal class LibContentAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser,
    @Named(NAMED_LIB_NAME)
    private val libName: String?
) : Analyzer {
    override fun process(): Report {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        return generateReport(processedData.contributors)
    }

    private fun generateReport(contributors: Set<Contributor>): Report {
        val library = contributors.find { File(it.originalOwner.path).nameWithoutExtension == libName }
            ?: throw RuntimeException("Can not find the $libName")
        val resourceRows = library.resources.toReportRows("Resource")
        val assetRows = library.assets.toReportRows("Asset")
        val nativeLibRows = library.nativeLibs.toReportRows("Native")
        val otherRows = library.others.toReportRows("Other")
        // todo : calculating the download sizer from the analytic process
        val classRows = library.classes
            .toReportRows("Class")
        return Report(
            id = LIB_CONTENT_METRICS_ID,
            name = LIB_CONTENT_METRICS_ID,
            rows = resourceRows + assetRows + nativeLibRows + otherRows + classRows,
        )
    }

    private fun Collection<FileInfo>.toReportRows(type: String): List<Row> = map {
        createRow(
            rowName = it.name,
            name = it.name,
            value = it.size,
            tag = type
        )
    }

}