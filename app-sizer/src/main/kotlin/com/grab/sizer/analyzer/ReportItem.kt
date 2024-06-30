package com.grab.sizer.analyzer


internal data class ReportItem(
    val id: String,
    val totalDownloadSize: Long,
    val name: String = "",
    val extraInfo: String = "",
    val owner: String? = null,
    val classesDownloadSize: Long = 0L,
    val nativeLibDownloadSize: Long = 0L,
    val resourceDownloadSize: Long = 0L,
    val assetDownloadSize: Long = 0L,
    val otherDownloadSize: Long = 0L,
)