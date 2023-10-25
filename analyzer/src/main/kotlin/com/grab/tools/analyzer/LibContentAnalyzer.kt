package com.grab.tools.analyzer

import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.report.LibContentReport
import javax.inject.Inject

class LibContentAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val reporter: LibContentReport,
    private val dataParser: DataParser
) : Analyzer {
    override fun process() {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        reporter.report(dataParser.apks, processedData.contributors)
    }


}