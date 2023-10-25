package com.grab.tools.analyzer

import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.di.*
import com.grab.tools.report.ApkAnalyticReport
import javax.inject.Inject

class ApkAnalyzer @Inject constructor(
    private val apkComponentAnalytic: ApkComponentProcessor,
    private val apkAnalyticReport: ApkAnalyticReport,
    private val dataParser: DataParser
) : Analyzer {
    override fun process() {
        val processedData = apkComponentAnalytic.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        apkAnalyticReport.report(dataParser.apks, processedData.contributors)
    }
}