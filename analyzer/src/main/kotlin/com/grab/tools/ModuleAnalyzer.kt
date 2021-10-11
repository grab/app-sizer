package com.grab.tools

import com.grab.tools.aar.AarFileParser
import com.grab.tools.analyzer.ApkComponentProcessor
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingParser
import com.grab.tools.di.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.report.ModuleAnalyticReport
import com.grab.tools.utils.DefaultAarFileQuery
import com.grab.tools.utils.DefaultJarFileQuery
import com.grab.tools.utils.ModuleAarFileQuery
import com.grab.tools.utils.ModuleJarFileQuery
import java.io.File
import javax.inject.Inject

@AppScope
class ModuleAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val proguardMappingParser: ProguardMappingParser,
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val libAarFileQuery: DefaultAarFileQuery,
    private val libJarFileQuery: DefaultJarFileQuery,
    private val moduleAarFileQuery: ModuleAarFileQuery,
    private val moduleJarFileQuery: ModuleJarFileQuery,
    private val analyticReport: ModuleAnalyticReport,
    @AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE)
    private val proguardMappingFile: File?,
    @AnalyzerInputFile(INPUT_FILE_ROOT_PROJECT)
    private val projectDir: File?,
    @AnalyzerInputFile(INPUT_FILE_LIB_DIRECTORY)
    private val librariesDirectory: File?,
    @AnalyzerInputFile(INPUT_FILE_APK_DIRECTORY)
    private val apkDirs: File,
) : Analyzer {
    override fun process() {
        if (librariesDirectory == null || projectDir == null)
            throw IllegalArgumentException("Libraries or project root directory is null")

        val proguardMap = proguardMappingFile?.run { proguardMappingParser.parse(this) }
        val apkFilesInfo = apkFileParser.parseApks(apkDirs, proguardMap)

        val aarFilesInfo = aarFileParser.parseAars(projectDir, moduleAarFileQuery)
        val jarFilesInfo = jarFileParser.parseJars(projectDir, moduleJarFileQuery)

        val libAarFilesInfo = aarFileParser.parseAars(librariesDirectory, libAarFileQuery)
        val libJarFilesInfo = jarFileParser.parseJars(librariesDirectory, libJarFileQuery)

        /**
         * Process the whole project to get the app module information
         */
        val wholeProject =
            apkComponentProcessor.process(apkFilesInfo, libAarFilesInfo + aarFilesInfo, libJarFilesInfo + jarFilesInfo)
        val appModule = Contributor(
            path = "root/app/build/",
            assets = wholeProject.noOwnerAssets.castToRawFile(),
            resources = wholeProject.noOwnerResources.castToRawFile(),
            nativeLibs = wholeProject.noOwnerNativeLibs.castToRawFile(),
            classes = wholeProject.noOwnerClasses.castToClass(),
            //others = wholeProject.noOwnerOthers.castToRawFile()
        )

        val processedData = apkComponentProcessor.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        analyticReport.report(apkFilesInfo, processedData.contributors + appModule)
    }
}