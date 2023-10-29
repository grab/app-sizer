package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.report.HybridField
import com.grab.tools.analyzer.report.Report
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.analyzer.report.Row
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.analyzer.model.Contributor
import com.grab.tools.analyzer.report.dexDownloadRatio
import com.grab.tools.parser.DataParser
import javax.inject.Inject

internal const val METRICS_ID_BASIC = "mobile.pax.app.size.components5"

internal class BasicApkAnalyzer @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider,
    private val dataParser: DataParser
) : Analyzer {
    override fun process() {
        report(dataParser.apks, setOf())
    }

    private fun report(androidBinaryInfo: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = androidBinaryInfo.dexDownloadRatio()
        reportWriters.forEach {
            it.write(
                AnalyticsOption.BASIC.name.toLowerCase(),
                Report(
                    projectInfo = projectInfoProvider.get(),
                    rows = androidBinaryInfo.createApkReportRows(dexCompressedRatio),
                    id = METRICS_ID_BASIC,
                    name = METRICS_ID_BASIC
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