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
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.Report
import java.io.File
import javax.inject.Inject


/**
 * An implementation of the Analyzer interface, focused on analyzing all libraries within the project.
 * This class is designed to handle [com.grab.sizer.AnalyticsOption.LIBRARIES].
 * The resulting report lists all libraries in the project along with their respective contributions to the total app download size.
 *
 * @property apkComponentProcessor An instance for processing APK, AAR, or JAR files to produce a list of contributors.
 * @property dataParser Parses APK, AAR, and JAR files for analysis.
 */
internal class LibrariesAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser
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
        val contributorList = contributors.sortedBy { it.getDownloadSize() }
        val listOfReport = reportPerLibrary(contributorList)
        return Report(
            id = LIBRARY_METRICS_ID,
            name = LIBRARY_METRICS_ID,
            rows = listOfReport.map { reportItem -> createRow(reportItem.name, reportItem.totalDownloadSize) },
        )
    }

    private fun Contributor.toReportItem(): ReportItem = ReportItem(
        name = tag,
        extraInfo = path.substring(path.indexOf("files-2.1/") + 9),
        id = File(path).nameWithoutExtension,
        totalDownloadSize = getDownloadSize(),
        classesDownloadSize = classDownloadSize,
        nativeLibDownloadSize = nativeLibDownloadSize,
        resourceDownloadSize = resourcesDownloadSize,
        assetDownloadSize = assetsDownloadSize,
        otherDownloadSize = othersDownloadSize,
    )

    private fun reportPerLibrary(data: List<Contributor>): List<ReportItem> =
        data.map { it.toReportItem() }
}