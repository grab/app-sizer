package com.grab.tools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.tools.apk.ProguardMappingParser
import com.grab.tools.report.GroupByLibAnalyticReport
import java.io.File

private const val OPTION_GROUP_BY_LIB = "group-by-lib"
private const val OPTION_SO_FILE_ONLY = "so"
private const val OPTION_RESOURCE_FILE_ONLY = "resource"

class BundleCommand : CliktCommand() {

    private val apkDirs: File by option(
        "-i",
        "--input-apks",
        help = "Path to the input apks directory"
    ).convert { File(it) }.required()

    private val libDirs: File by option(
        "-a",
        "--aar-dir",
        help = "Path to the aar files"
    ).convert { File(it) }.required()

    private val output: File by option(
        "-o",
        "--output-file",
        help = "Path to the output excel file"
    ).convert { File(it) }.required()

    private val mappingFile: File by option(
        "-m",
        "--mapping-file",
        help = "Path to the R8 mapping file"
    ).convert { File(it) }.required()

    private val reportOption by option()
        .switch(
            "--group-by-lib" to OPTION_GROUP_BY_LIB,
            "--so" to OPTION_SO_FILE_ONLY,
            "--res" to OPTION_RESOURCE_FILE_ONLY,
        ).default(OPTION_GROUP_BY_LIB)

    override fun run() {
        val apkProfileComponent = DaggerApkProfileComponent.create()
        val apkComponentAnalytic = apkProfileComponent.apkComponentAnalytic()
        val proguardMap = ProguardMappingParser().parse(mappingFile)
        val aarFilesInfo = apkProfileComponent.aarFileParser().parseAars(libDirs)
        val jarFilesInfo = apkProfileComponent.jarFileParser().parseJars(libDirs)
        val apkFilesInfo = apkProfileComponent.apkParser().parseApks(apkDirs, proguardMap)
        val processedData = apkComponentAnalytic.process(apkFilesInfo, aarFilesInfo, jarFilesInfo)

        when (reportOption) {
            OPTION_GROUP_BY_LIB -> GroupByLibAnalyticReport(output)
                .report(apkFilesInfo, processedData)
        }
    }
}