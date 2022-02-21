package com.grab.tools.analyzer

import com.grab.tools.aar.AarFileParser
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingParser
import com.grab.tools.di.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.processor.ApkComponentProcessor
import com.grab.tools.report.ApkAnalyticReport
import com.grab.tools.utils.DefaultAarFileQuery
import com.grab.tools.utils.DefaultJarFileQuery
import java.io.File
import javax.inject.Inject

@AppScope
class ApkAnalyzer @Inject constructor(
    private val apkComponentAnalytic: ApkComponentProcessor,
    private val proguardMappingParser: ProguardMappingParser,
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val aarFileQuery: DefaultAarFileQuery,
    private val jarFileQuery: DefaultJarFileQuery,
    private val apkAnalyticReport: ApkAnalyticReport,
    @AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE)
    private val proguardMappingFile: File?,
    @AnalyzerInputFile(INPUT_FILE_LIB_DIRECTORY)
    private val librariesDirectory: File?,
    @AnalyzerInputFile(INPUT_FILE_APK_DIRECTORY)
    private val apkDirs: File,
) : Analyzer {
    override fun process() {
        if (librariesDirectory == null)
            throw IllegalArgumentException("Libraries or project root directory is null")

        val proguardMap = proguardMappingFile?.run { proguardMappingParser.parse(this) }
        val apkFilesInfo = apkFileParser.parseApks(apkDirs, proguardMap)

        val aarFilesInfo = aarFileParser.parseAars(librariesDirectory, aarFileQuery)
        val jarFilesInfo = jarFileParser.parseJars(librariesDirectory, jarFileQuery)
        val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        apkAnalyticReport.report(apkFilesInfo, processedData.contributors)
    }
}