package com.grab.tools.report

import com.grab.tools.analyzer.report.HybridField
import com.grab.tools.analyzer.report.Report
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.analyzer.report.Row
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import javax.inject.Inject

class BasicApkAnalyticReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider
) : AnalyticReport {

    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = androidBinaryInfo.dexDownloadRatio()
        reportWriters.forEach {
            it.write(
                Report(
                    projectInfo = projectInfoProvider.get(),
                    rows = androidBinaryInfo.createApkReportRows(dexCompressedRatio),
                    id = METRICS_ID_BASIC_APK,
                    name = METRICS_ID_BASIC_APK
                )
            )
        }
    }

    private fun Set<ApkFileInfo>.createApkReportRows(dexCompressedRatio: Double): List<Row> {
        val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
        val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
        val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
        val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
        val dexDownloadFile = flatMap { it.dexes }.sumOf { it.downloadSize }

        val classesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
        val total =
            resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + classDownloadSize

        return listOf(
            createRow(
                name = "apk",
                value = total
            ),
            createRow(
                name = "resource",
                value = resourceDownloadSize
            ),
            createRow(
                name = "native_lib",
                value = nativeLibDownloadSize
            ),
            createRow(
                name = "asset",
                value = assetDownloadSize
            ),
            createRow(
                name = "other",
                value = otherDownloadSize
            ),
            createRow(
                name = "code",
                value = dexDownloadFile
            )
        )
    }

    private fun createRow(name: String, value: Long): Row = Row(
        fields = listOf(
            HybridField(
                name = name,
                value = value
            )
        ),
        name = name
    )
}
