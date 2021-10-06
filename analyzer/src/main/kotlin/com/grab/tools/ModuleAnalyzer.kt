package com.grab.tools

import com.grab.tools.aar.AarFileParser
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingParser
import com.grab.tools.di.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.report.ModuleAnalyticReport
import com.grab.tools.utils.ModuleAarFileQuery
import com.grab.tools.utils.ModuleJarFileQuery
import java.io.File
import javax.inject.Inject

@AppScope
class ModuleAnalyzer @Inject constructor(
    private val apkComponentAnalytic: ApkComponentAnalytic,
    private val proguardMappingParser: ProguardMappingParser,
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val moduleAarFileQuery: ModuleAarFileQuery,
    private val moduleJarFileQuery: ModuleJarFileQuery,
    private val analyticReport: ModuleAnalyticReport,
    @AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE)
    private val proguardMappingFile: File?,
    @AnalyzerInputFile(INPUT_FILE_LIB_DIRECTORY)
    private val projectDir: File?,
    @AnalyzerInputFile(INPUT_FILE_APK_DIRECTORY)
    private val apkDirs: File,
) : Analyzer {
    override fun process() {
        if (projectDir == null)
            throw IllegalArgumentException("Project root directory is null")

        val proguardMap = proguardMappingFile?.run { proguardMappingParser.parse(this) }
        val apkFilesInfo = apkFileParser.parseApks(apkDirs, proguardMap)

        val aarFilesInfo = aarFileParser.parseAars(projectDir, moduleAarFileQuery)
        val jarFilesInfo = jarFileParser.parseJars(projectDir, moduleJarFileQuery)
        val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        analyticReport.report(apkFilesInfo, processedData)
    }
}