package com.grab.tools.analyzer

import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingParser
import com.grab.tools.di.AnalyzerInputFile
import com.grab.tools.di.AppScope
import com.grab.tools.di.INPUT_FILE_APK_DIRECTORY
import com.grab.tools.di.INPUT_FILE_PROGUARD_MAPPING_FILE
import com.grab.tools.report.BasicApkAnalyticReport
import java.io.File
import javax.inject.Inject

@AppScope
class BasicApkAnalyzer @Inject constructor(
    private val proguardMappingParser: ProguardMappingParser,
    private val apkFileParser: ApkFileParser,
    private val analyticReport: BasicApkAnalyticReport,
    @AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE)
    private val proguardMappingFile: File?,
    @AnalyzerInputFile(INPUT_FILE_APK_DIRECTORY)
    private val apkDirs: File,
) : Analyzer {
    override fun process() {
        val proguardMap = proguardMappingFile?.run { proguardMappingParser.parse(this) }
        val apkFilesInfo = apkFileParser.parseApks(apkDirs, proguardMap)
        analyticReport.report(apkFilesInfo, setOf())
    }
}