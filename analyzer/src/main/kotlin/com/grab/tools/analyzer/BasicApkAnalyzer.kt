package com.grab.tools.analyzer

import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingProvider
import com.grab.tools.di.AppScope
import com.grab.tools.report.BasicApkAnalyticReport
import com.grab.tools.utils.InputFileProvider
import javax.inject.Inject

@AppScope
class BasicApkAnalyzer @Inject constructor(
    private val proguardMappingProvider: ProguardMappingProvider,
    private val apkFileParser: ApkFileParser,
    private val analyticReport: BasicApkAnalyticReport,
    private val inputFileProvider: InputFileProvider,
) : Analyzer {
    override fun process() {
        val apkFilesInfo = apkFileParser.parseApks(
            inputFileProvider.provideApkFiles(),
            proguardMappingProvider.provide()
        )
        analyticReport.report(apkFilesInfo, setOf())
    }
}