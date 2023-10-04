package com.grab.tools.analyzer

import com.grab.tools.aar.AarFileParser
import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingProvider
import com.grab.tools.di.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.report.ApkAnalyticReport
import com.grab.tools.utils.InputFileProvider
import javax.inject.Inject

@AppScope
class ApkAnalyzer @Inject constructor(
    private val apkComponentAnalytic: ApkComponentProcessor,
    private val proguardMappingProvider: ProguardMappingProvider,
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val apkAnalyticReport: ApkAnalyticReport,
    private val inputFileProvider: InputFileProvider
) : Analyzer {
    override fun process() {
        val proguardMap = proguardMappingProvider.provide()
        val apkFilesInfo = apkFileParser.parseApks(inputFileProvider.provideApkFiles(), proguardMap)

        val aarFilesInfo = aarFileParser.parseAars(inputFileProvider.provideLibraryAar())
        val jarFilesInfo = jarFileParser.parseJars(inputFileProvider.provideLibraryJar())
        val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        apkAnalyticReport.report(apkFilesInfo, processedData.contributors)
    }
}