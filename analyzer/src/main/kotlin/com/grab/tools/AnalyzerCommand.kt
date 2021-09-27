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

    private val librariesDir: File? by option(
        "-l",
        "--lib-dir",
        help = "Path to the directory contains all libs (aar & jar) files"
    ).convert { File(it) }

    private val projectDir: File? by option(
        "-p",
        "--project-dir",
        help = "Path to the project's root folder"
    ).convert { File(it) }

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
            "--general" to AnalyticsOption.GENERAL,
        ).default(AnalyticsOption.LIBRARIES_ANALYTICS)

    override fun run() {
        val component = DaggerAnalyzerComponent.factory()
            .create(
                libsDir = librariesDir,
                rootProjectDir = projectDir,
                featureMappingFile = featureMappingFile,
                output = outputFile,
                analyticsOption = reportOption
            )
        val apkComponentAnalytic = component.apkComponentAnalytic()
        val analyticReportMap = component.analyticReportMap()
        val jarFileQueryMap = component.jarFileQueryMap()
        val aarFileQueryMap = component.aarFileQueryMap()
        val proguardMap = ProguardMappingParser().parse(mappingFile)
        val apkFilesInfo = component.apkParser().parseApks(apkDirs, proguardMap)
        val libDir = librariesDir
        val projectDir = projectDir
        when {
            // Todo move to a separate class
            reportOption == AnalyticsOption.GENERAL && libDir != null && projectDir != null -> {
                val libAarFileQuery = aarFileQueryMap[AnalyticsOption.LIBRARIES_ANALYTICS]!!
                val libJarFileQuery = jarFileQueryMap[AnalyticsOption.LIBRARIES_ANALYTICS]!!
                val libAarFilesInfo = component.aarFileParser().parseAars(libDir, libAarFileQuery)
                val libJarFilesInfo = component.jarFileParser().parseJars(libDir, libJarFileQuery)
                val libProcessedData = apkComponentAnalytic.process(apkFilesInfo, libAarFilesInfo, libJarFilesInfo)
                val allLibContributor = libProcessedData.reduce { acc, contributor ->
                    Contributor(
                        path = "All-libraries/build/",
                        assets = acc.assets + contributor.assets,
                        resources = acc.resources + contributor.resources,
                        nativeLibs = acc.nativeLibs + contributor.nativeLibs,
                        classes = acc.classes + contributor.classes,
                        others = acc.others + contributor.others
                    )
                }

                val aarFileQuery = aarFileQueryMap[AnalyticsOption.FEATURES_ANALYTICS]!!
                val jarFileQuery = jarFileQueryMap[AnalyticsOption.FEATURES_ANALYTICS]!!
                val aarFilesInfo = component.aarFileParser().parseAars(projectDir, aarFileQuery)
                val jarFilesInfo = component.jarFileParser().parseJars(projectDir, jarFileQuery)
                val processedData =
                    apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo) + allLibContributor
                analyticReportMap[AnalyticsOption.FEATURES_ANALYTICS]?.report(apkFilesInfo, processedData)
            }
            reportOption == AnalyticsOption.LIBRARIES_ANALYTICS && libDir != null -> {
                val aarFileQuery = aarFileQueryMap[AnalyticsOption.LIBRARIES_ANALYTICS]
                val jarFileQuery = jarFileQueryMap[AnalyticsOption.LIBRARIES_ANALYTICS]
                if (aarFileQuery != null && jarFileQuery != null) {
                    val aarFilesInfo = component.aarFileParser().parseAars(libDir, aarFileQuery)
                    val jarFilesInfo = component.jarFileParser().parseJars(libDir, jarFileQuery)
                    val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
                    analyticReportMap[AnalyticsOption.LIBRARIES_ANALYTICS]?.report(apkFilesInfo, processedData)
                }
            }
            reportOption == AnalyticsOption.FEATURES_ANALYTICS && projectDir != null -> {
                val aarFileQuery = aarFileQueryMap[AnalyticsOption.FEATURES_ANALYTICS]
                val jarFileQuery = jarFileQueryMap[AnalyticsOption.FEATURES_ANALYTICS]
                if (aarFileQuery != null && jarFileQuery != null) {
                    val aarFilesInfo = component.aarFileParser().parseAars(projectDir, aarFileQuery)
                    val jarFilesInfo = component.jarFileParser().parseJars(projectDir, jarFileQuery)
                    val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)
                    analyticReportMap[AnalyticsOption.FEATURES_ANALYTICS]?.report(apkFilesInfo, processedData)
                }
            }
        }
    }
}

