package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.analyzer.report.HybridField
import com.grab.tools.analyzer.report.Report
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.analyzer.report.Row
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import com.grab.tools.report.ReportItem
import com.grab.tools.report.apksSizeReport
import com.grab.tools.report.dexDownloadRatio
import java.io.File
import javax.inject.Inject

internal const val LIBRARY_METRICS_ID = "mobile.pax.app.size.lib5"

internal class LibrariesAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider,
    private val dataParser: DataParser
) : Analyzer {
    override fun process() {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        report(dataParser.apks, processedData.contributors)
    }

    private fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val contributorList = contributors.sortedBy { it.getDownloadSize(dexCompressedRatio) }
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val totalLibsReport = totalLibrariesReport(dexCompressedRatio, contributorList)
        val codeBaseReport = codeBaseReport(totalLibsReport, apkReport)

        val listOfReport =
            listOf(apkReport, codeBaseReport, totalLibsReport) + reportPerLibrary(dexCompressedRatio, contributorList)
        reportWriters.forEach {
            it.write(
                AnalyticsOption.LIBRARIES.name.toLowerCase(),
                Report(
                    projectInfo = projectInfoProvider.get(),
                    id = LIBRARY_METRICS_ID,
                    name = LIBRARY_METRICS_ID,
                    rows = listOfReport.toReportRows()
                )
            )
        }
    }

    private fun List<ReportItem>.toReportRows() =
        map { reportItem ->
            Row(
                name = reportItem.name,
                fields = listOf(
                    HybridField(
                        name = reportItem.name,
                        value = reportItem.totalDownloadSize,
                        tag = reportItem.extraInfo
                    )
                )
            )
        }

    private fun codeBaseReport(
        totalLibsReport: ReportItem,
        apkReport: ReportItem
    ): ReportItem = ReportItem(
        id = CODE_BASE_ID,
        name = CODE_BASE_ID,
        totalDownloadSize = apkReport.totalDownloadSize - totalLibsReport.totalDownloadSize,
        otherDownloadSize = apkReport.otherDownloadSize - totalLibsReport.otherDownloadSize,
        resourceDownloadSize = apkReport.resourceDownloadSize - totalLibsReport.resourceDownloadSize,
        nativeLibDownloadSize = apkReport.nativeLibDownloadSize - totalLibsReport.nativeLibDownloadSize,
        classesDownloadSize = apkReport.classesDownloadSize - totalLibsReport.classesDownloadSize,
        classesSize = apkReport.classesSize - totalLibsReport.classesSize
    )

    private fun Contributor.toReportItem(dexCompressedRatio: Double): ReportItem = ReportItem(
        name = File(path).nameWithoutExtension,
        extraInfo = path.substring(path.indexOf("files-2.1/") + 9),
        id = File(path).nameWithoutExtension,
        totalDownloadSize = getDownloadSize(dexCompressedRatio),
        classesDownloadSize = getClassDownloadSize(dexCompressedRatio),
        classesSize = classSize,
        nativeLibDownloadSize = nativeLibDownloadSize,
        resourceDownloadSize = resourcesDownloadSize,
        assetDownloadSize = assetsDownloadSize,
        otherDownloadSize = othersDownloadSize,
    )

    private fun totalLibrariesReport(dexCompressedRatio: Double, data: List<Contributor>): ReportItem {
        return data.reduce { pre, cur ->
            pre.copy(
                resources = pre.resources + cur.resources,
                assets = pre.assets + cur.assets,
                nativeLibs = pre.nativeLibs + cur.nativeLibs,
                classes = pre.classes + cur.classes,
                others = pre.others + cur.others
            )
        }.toReportItem(dexCompressedRatio)
            .copy(
                name = "All libraries",
                extraInfo = "Sum up all libraries values",
                id = "all_libraries",
            )
    }

    private fun reportPerLibrary(dexCompressedRatio: Double, data: List<Contributor>): List<ReportItem> =
        data.map { it.toReportItem(dexCompressedRatio) }
}