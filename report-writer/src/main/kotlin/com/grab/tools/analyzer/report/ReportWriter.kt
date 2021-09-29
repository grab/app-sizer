package com.grab.tools.analyzer.report

data class ReportItem(
    val id: String,
    val name: String,
    val extraInfo: String,
    val totalDownloadSize: Long,
    val classesSize: Long,
    val classesDownloadSize: Long,
    val nativeLibDownloadSize: Long,
    val resourceDownloadSize: Long,
    val assetDownloadSize: Long,
    val otherDownloadSize: Long
)

data class AppInfo(
    val versionName: String,
    val deviceName : String
)

interface ReportWriter {
    fun write(appInfo: AppInfo, report: List<ReportItem>)
}