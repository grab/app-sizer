package com.grab.tools.analyzer

import com.grab.tools.aar.AarFileParser
import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ProguardMappingProvider
import com.grab.tools.di.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.model.Contributor
import com.grab.tools.model.castToClass
import com.grab.tools.model.castToRawFile
import com.grab.tools.report.LargeFileReport
import com.grab.tools.utils.*
import javax.inject.Inject

@AppScope
class LargeFileAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val proguardMappingProvider: ProguardMappingProvider,
    private val inputFileProvider: InputFileProvider,
    private val analyticReport: LargeFileReport,
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
) : Analyzer {
    override fun process() {
        val proguardMap = proguardMappingProvider.provide()
        val apkFilesInfo = apkFileParser.parseApks(inputFileProvider.provideApkFiles(), proguardMap)

        val aarFilesInfo = aarFileParser.parseAars(inputFileProvider.provideModuleAar())
        val jarFilesInfo = jarFileParser.parseJars(inputFileProvider.provideModuleJar())

        val libAarFilesInfo = aarFileParser.parseAars(inputFileProvider.provideLibraryAar())
        val libJarFilesInfo = jarFileParser.parseJars(inputFileProvider.provideLibraryJar())

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
        )

        val processedData = apkComponentProcessor.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        analyticReport.report(apkFilesInfo, processedData.contributors + appModule)
    }
}