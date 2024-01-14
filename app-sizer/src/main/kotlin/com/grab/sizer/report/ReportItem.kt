package com.grab.sizer.report


@Deprecated("A new report API have built. Use com.grab.sizer.analyzer.report.Field instead")
data class ReportItem(
    val id: String,
    val totalDownloadSize: Long,
    val name: String = "",
    val extraInfo: String = "",
    val owner: String? = null,
    val classesSize: Long = 0L,
    val classesDownloadSize: Long = 0L,
    val nativeLibDownloadSize: Long = 0L,
    val resourceDownloadSize: Long = 0L,
    val assetDownloadSize: Long = 0L,
    val otherDownloadSize: Long = 0L,
)