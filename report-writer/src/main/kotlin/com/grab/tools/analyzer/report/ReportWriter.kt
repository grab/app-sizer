package com.grab.tools.analyzer.report

data class ReportItem(
    val id: String,
    val totalDownloadSize: Long,
    val name: String = "",
    val extraInfo: String = "",
    val classesSize: Long = 0L,
    val classesDownloadSize: Long = 0L,
    val nativeLibDownloadSize: Long = 0L,
    val resourceDownloadSize: Long = 0L,
    val assetDownloadSize: Long = 0L,
    val otherDownloadSize: Long = 0L
)

data class AppInfo(
    val versionName: String,
    val deviceName : String,
    val tag: String = ""
)

interface ReportWriter {
    fun write(appInfo: AppInfo, report: List<ReportItem>)
}