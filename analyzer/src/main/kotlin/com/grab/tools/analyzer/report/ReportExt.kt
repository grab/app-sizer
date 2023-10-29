package com.grab.tools.analyzer.report

import com.grab.tools.parser.ApkFileInfo

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

internal fun Set<ApkFileInfo>.toReportField(dexCompressedRatio: Double): Field {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val classesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
    val total =
        resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + classDownloadSize

    return HybridField(name = "apk", value = total)
}

private const val DEFAULT_VERSION_NAME = "0.0.0"
internal fun Set<ApkFileInfo>.getVersionName(): String =
    find { it.manifestFileInfo.versionName != null }?.manifestFileInfo?.versionName ?: DEFAULT_VERSION_NAME


internal fun Set<ApkFileInfo>.dexDownloadRatio(): Double {
    val dexDownloadSize = flatMap { it.dexes }.sumOf { it.downloadSize }
    val dexClassesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    return dexDownloadSize.toDouble() / dexClassesSize
}