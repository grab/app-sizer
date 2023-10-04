package com.grab.tools.analyzer

import com.grab.tools.aar.AarFileParser
import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingProvider
import com.grab.tools.di.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.report.LibrariesAnalyticReport
import com.grab.tools.utils.InputFileProvider
import javax.inject.Inject

@AppScope
class LibrariesAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val proguardMappingProvider: ProguardMappingProvider,
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val inputFileProvider: InputFileProvider,
    private val librariesAnalyticReport: LibrariesAnalyticReport
) : Analyzer {
    override fun process() {
        val proguardMap = proguardMappingProvider.provide()
        val apkFilesInfo = apkFileParser.parseApks(inputFileProvider.provideApkFiles(), proguardMap)

        val aarFilesInfo = aarFileParser.parseAars(inputFileProvider.provideLibraryAar())
        val jarFilesInfo = jarFileParser.parseJars(inputFileProvider.provideLibraryJar())

        val processedData = apkComponentProcessor.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        librariesAnalyticReport.report(apkFilesInfo, processedData.contributors)
    }
}