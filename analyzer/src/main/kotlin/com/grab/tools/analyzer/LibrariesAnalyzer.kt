package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.mapper.ApkComponentProcessor
import com.grab.tools.analyzer.model.Contributor
import com.grab.tools.analyzer.report.*
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.parser.DataParser
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
        val listOfReport = reportPerLibrary(dexCompressedRatio, contributorList)
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

    private fun reportPerLibrary(dexCompressedRatio: Double, data: List<Contributor>): List<ReportItem> =
        data.map { it.toReportItem(dexCompressedRatio) }
}