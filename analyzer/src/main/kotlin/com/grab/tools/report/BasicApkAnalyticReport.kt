package com.grab.tools.report

import com.grab.tools.model.Contributor
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.di.NAMED_DEVICE_NAME
import javax.inject.Inject
import javax.inject.Named

class BasicApkAnalyticReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    @Named(NAMED_DEVICE_NAME)
    private val deviceName: String?
) : AnalyticReport {

    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = androidBinaryInfo.dexDownloadRatio()
        val apkSizeReport = androidBinaryInfo.apksSizeReport(dexCompressedRatio)
        val fragmentedReport = androidBinaryInfo.apksSizeBreakdownReport()
        reportWriters.forEach {
            it.write(
                androidBinaryInfo.toAppInfo(deviceName),
                listOf(apkSizeReport) + fragmentedReport,
                METRICS_ID_BASIC_APK
            )
        }
    }

    private fun Set<ApkFileInfo>.apksSizeBreakdownReport(): List<ReportItem> {
        val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
        val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
        val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
        val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
        val dexDownloadFile = flatMap { it.dexes }.sumOf { it.downloadSize }

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
                id = "code",
                totalDownloadSize = dexDownloadFile
            )
        )
    }
}