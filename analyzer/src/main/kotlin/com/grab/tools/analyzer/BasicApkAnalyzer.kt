package com.grab.tools.analyzer

import com.grab.tools.report.BasicApkAnalyticReport
import javax.inject.Inject

class BasicApkAnalyzer @Inject constructor(
    private val analyticReport: BasicApkAnalyticReport,
    private val dataParser: DataParser
) : Analyzer {
    override fun process() {
        analyticReport.report(dataParser.apks, setOf())
    }
}