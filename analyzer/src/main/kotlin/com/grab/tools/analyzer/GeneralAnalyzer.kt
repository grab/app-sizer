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
import com.grab.tools.report.FeatureAnalyticReport
import com.grab.tools.utils.*
import javax.inject.Inject

interface Analyzer {
    fun process()
}

@AppScope
class GeneralAnalyzer @Inject constructor(
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val apkComponentProcessor: ApkComponentProcessor,
    private val proguardMappingProvider: ProguardMappingProvider,
    private val inputFileProvider: InputFileProvider,
    private val featureAnalyticReport: FeatureAnalyticReport
) : Analyzer {
    override fun process() {
        val proguardMap = proguardMappingProvider.provide()
        val apkFilesInfo = apkFileParser.parseApks(inputFileProvider.provideApkFiles(), proguardMap)
        val libAarFilesInfo = aarFileParser.parseAars(inputFileProvider.provideLibraryAar())
        val libJarFilesInfo = jarFileParser.parseJars(inputFileProvider.provideLibraryJar())
        val aarFilesInfo = aarFileParser.parseAars(inputFileProvider.provideModuleAar())
        val jarFilesInfo = jarFileParser.parseJars(inputFileProvider.provideModuleJar())

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

        val modulesData =
            apkComponentProcessor.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        featureAnalyticReport.report(apkFilesInfo, modulesData.contributors + appModule)
    }
}