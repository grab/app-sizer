package com.grab.bundle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.bundle.report.BuildReportData
import com.grab.bundle.report.GroupByLibAnalyticReport
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

    private val aarDirs: File by option(
        "-a",
        "--aar-dir",
        help = "Path to the aar files"
    ).convert { File(it) }.required()

    private val output: File by option(
        "-o",
        "--output-file",
        help = "Path to the output excel file"
    ).convert { File(it) }.required()

    private val reportOption by option()
        .switch(
            "--group-by-lib" to OPTION_GROUP_BY_LIB,
            "--so" to OPTION_SO_FILE_ONLY,
            "--res" to OPTION_RESOURCE_FILE_ONLY,
        ).default(OPTION_GROUP_BY_LIB)

    override fun run() {
        val fileQuery = ApkComponentFactory.provideFileQuery()
        val apkFileParser = ApkComponentFactory.provideApkParser(fileQuery)
        val aarFileParser = ApkComponentFactory.provideAarFileParser(fileQuery)
        val analytics = ApkComponentFactory.provideAnalytics()
        val apkComponentAnalytic =
            ApkComponentFactory.provideApkComponentAnalytic(apkFileParser, aarFileParser, analytics)
        val processedData = apkComponentAnalytic.process(apkDirs, aarDirs)
        val buildReportData = BuildReportData(analytics)
        val apkInfo = apkFileParser.parseApks(apkDirs)
        when (reportOption) {
            OPTION_GROUP_BY_LIB -> GroupByLibAnalyticReport(buildReportData, output)
                .report(apkInfo, processedData)
        }
    }
}