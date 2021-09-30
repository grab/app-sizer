package com.grab.tools.report

import com.grab.tools.analyzer.report.AppInfo
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.apk.ApkFileInfo

internal fun Set<ApkFileInfo>.apksSizeReport(dexCompressedRatio: Double): ReportItem {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val classesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
    val total =
        resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + classDownloadSize
    return ReportItem(
        id = "apk",
        totalDownloadSize = total,
        name = "Apks",
        resourceDownloadSize = resourceDownloadSize,
        nativeLibDownloadSize = nativeLibDownloadSize,
        assetDownloadSize = assetDownloadSize,
        otherDownloadSize = otherDownloadSize,
        classesSize = classesSize,
        classesDownloadSize = classDownloadSize,
        extraInfo = "Apk breakdown by component size"
    )
}

private const val DEFAULT_VERSION_NAME = "0.0.0"
private const val DEFAULT_DEVICE_NAME = "PreferenceDevice"

internal fun Set<ApkFileInfo>.toAppInfo(deviceName: String?): AppInfo {
    val versionName =
        find { it.manifestFileInfo.versionName != null }?.manifestFileInfo?.versionName ?: DEFAULT_VERSION_NAME
    return AppInfo(
        versionName = versionName,
        deviceName = deviceName ?: DEFAULT_DEVICE_NAME,
    )
}

internal fun Set<ApkFileInfo>.dexDownloadRatio(): Double {
    val dexDownloadSize = flatMap { it.dexes }.sumOf { it.downloadSize }
    val dexClassesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    return dexDownloadSize.toDouble() / dexClassesSize
}