package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo

class ApkAnalyticReport(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val deviceName: String?
) : AnalyticReport {

    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>) {
        val dexCompressedRatio = androidBinaryInfo.dexDownloadRatio()
        val apkReport = androidBinaryInfo.apksSizeReport(dexCompressedRatio)
        val fragmentedReport = androidBinaryInfo.apksSizeBreakdownReport(dexCompressedRatio)
        reportWriters.forEach {
            it.write(androidBinaryInfo.toAppInfo(deviceName), listOf(apkReport) + fragmentedReport)
        }
    }

    internal fun Set<ApkFileInfo>.apksSizeBreakdownReport(dexCompressedRatio: Double): List<ReportItem> {
        val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
        val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
        val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
        val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
        val classesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
        return listOf(
            ReportItem(
                id = "resource",
                totalDownloadSize = resourceDownloadSize
            ),
            ReportItem(
                id = "native_lib",
                totalDownloadSize = nativeLibDownloadSize
            ),
            ReportItem(
                id = "asset",
                totalDownloadSize = assetDownloadSize
            ),
            ReportItem(
                id = "other",
                totalDownloadSize = otherDownloadSize
            ),
            ReportItem(
                id = "class",
                totalDownloadSize = classesSize
            ),
            ReportItem(
                id = "class_extracted",
                totalDownloadSize = classDownloadSize
            )
        )
    }
}