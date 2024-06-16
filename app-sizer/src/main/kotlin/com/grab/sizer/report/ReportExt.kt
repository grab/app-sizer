package com.grab.sizer.report

import com.grab.sizer.analyzer.ReportItem
import com.grab.sizer.parser.ApkFileInfo

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
        extraInfo = "Apk breakdown by component sizer"
    )
}

internal fun Set<ApkFileInfo>.toReportField(dexCompressedRatio: Double): List<Field> {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val classesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
    val total =
        resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + classDownloadSize
    return listOf(
        TagField(
            name = FIELD_KEY_CONTRIBUTOR,
            value = "apk"
        ),
        DefaultField(
            name = FIELD_KEY_SIZE,
            value = total
        )
    )
}


internal fun Set<ApkFileInfo>.dexDownloadRatio(): Double {
    val dexDownloadSize = flatMap { it.dexes }.sumOf { it.downloadSize }
    val dexClassesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    return dexDownloadSize.toDouble() / dexClassesSize
}