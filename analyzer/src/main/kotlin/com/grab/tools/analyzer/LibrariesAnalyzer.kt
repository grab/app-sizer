package com.grab.tools.analyzer

import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.di.*
import com.grab.tools.report.LibrariesAnalyticReport
import javax.inject.Inject

class LibrariesAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val librariesAnalyticReport: LibrariesAnalyticReport,
    private val dataParser: DataParser
) : Analyzer {
    override fun process() {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        librariesAnalyticReport.report(dataParser.apks, processedData.contributors)
    }
}