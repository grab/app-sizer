package com.grab.sizer.report

import com.grab.sizer.analyzer.ReportItem
import com.grab.sizer.parser.ApkFileInfo

internal fun Set<ApkFileInfo>.apksSizeReport(): ReportItem {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val classDownloadSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.downloadSize }
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
        classesDownloadSize = classDownloadSize,
        extraInfo = "Apk breakdown by component sizer"
    )
}

internal fun Set<ApkFileInfo>.toReportField(): List<Field> {
    val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
    val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
    val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
    val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
    val classDownloadSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.downloadSize }
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