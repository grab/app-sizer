package com.grab.tools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.tools.apk.ProguardMappingParser
import com.grab.tools.di.DaggerAnalyzerComponent
import java.io.File


class AnalyzerCommand : CliktCommand() {

    private val apkDirs: File by option(
        "-a",
        "--apk-dir",
        help = "Path to the input apks directory"
    ).convert { File(it) }.required()

    private val inputDir: File by option(
        "-i",
        "--input-dir",
        help = "Path to the directory contains all libs (aar & jar) files or the project root folder"
    ).convert { File(it) }.required()

    private val outputFile: File by option(
        "-o",
        "--output-file",
        help = "Path to the output excel file"
    ).convert { File(it) }.required()

    private val mappingFile: File by option(
        "-m",
        "--mapping-file",
        help = "Path to the R8/Proguard mapping file"
    ).convert { File(it) }.required()

    private val featureMappingFile: File? by option(
        "-f",
        "--feature-mapping-file",
        help = "An yml file to grouped the feature's modules"
    ).convert { File(it) }

    private val reportOption by option()
        .switch(
            "--libraries" to AnalyticsOption.LIBRARIES_ANALYTICS,
            "--modules" to AnalyticsOption.MODULE_ANALYTICS,
            "--features" to AnalyticsOption.FEATURES_ANALYTICS,
            "--apk" to AnalyticsOption.APK_ANALYTICS,
        ).default(AnalyticsOption.LIBRARIES_ANALYTICS)

    override fun run() {
        val component = DaggerAnalyzerComponent.factory()
            .create(inputDir, outputFile, featureMappingFile, reportOption)
        val apkComponentAnalytic = component.apkComponentAnalytic()
        val proguardMap = ProguardMappingParser().parse(mappingFile)
        val aarFilesInfo = component.aarFileParser().parseAars(inputDir)
        val jarFilesInfo = component.jarFileParser().parseJars(inputDir)
        val apkFilesInfo = component.apkParser().parseApks(apkDirs, proguardMap)
        val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
        component.analyticReportMap()[reportOption]?.report(apkFilesInfo, processedData)
    }

}