package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.Report
import com.grab.sizer.report.ReportItem
import com.grab.sizer.report.dexDownloadRatio
import java.io.File
import javax.inject.Inject


/**
 * An implementation of the Analyzer interface, focused on analyzing all libraries within the project.
 * This class is designed to handle [com.grab.sizer.AnalyticsOption.LIBRARIES].
 * The resulting report lists all libraries in the project along with their respective contributions to the total app download size.
 *
 * @property apkComponentProcessor An instance for processing APK, AAR, or JAR files to produce a list of contributors.
 * @property dataParser Parses APK, AAR, and JAR files for analysis.
 */
internal class LibrariesAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser
) : Analyzer {
    override fun process(): Report {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        return generateReport(dataParser.apks, processedData.contributors)
    }

    private fun generateReport(apks: Set<ApkFileInfo>, contributors: Set<Contributor>): Report {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val contributorList = contributors.sortedBy { it.getDownloadSize(dexCompressedRatio) }
        val listOfReport = reportPerLibrary(dexCompressedRatio, contributorList)
        return Report(
            id = LIBRARY_METRICS_ID,
            name = LIBRARY_METRICS_ID,
            rows = listOfReport.map { reportItem -> createRow(reportItem.name, reportItem.totalDownloadSize) },
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