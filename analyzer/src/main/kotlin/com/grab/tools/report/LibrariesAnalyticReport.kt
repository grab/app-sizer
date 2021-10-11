package com.grab.tools.report

import com.grab.tools.analyzer.ComponentProcessorResult
import com.grab.tools.Contributor
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.di.NAMED_DEVICE_NAME
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named

private const val CODE_BASE_ID = "Codebase"

class LibrariesAnalyticReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    @Named(NAMED_DEVICE_NAME)
    private val deviceName: String?
) : AnalyticReport {
    override fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = dexDownloadRatio(apks)
        val contributorList = sortContributors(dexCompressedRatio, contributors)
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val totalLibsReport = totalLibrariesReport(dexCompressedRatio, contributorList)
        val codeBaseReport = codeBaseReport(totalLibsReport, apkReport)
        val listOfReport =
            listOf(apkReport, codeBaseReport, totalLibsReport) + reportPerLibrary(dexCompressedRatio, contributorList)
        reportWriters.forEach { it.write(apks.toAppInfo(deviceName), listOfReport, LIBRARY_METRICS_ID) }
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

    private fun dexDownloadRatio(apks: Set<ApkFileInfo>): Double {
        val dexDownloadSize = apks.flatMap { it.dexes }.sumOf { it.downloadSize }
        val dexClassesSize = apks.flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        return dexDownloadSize.toDouble() / dexClassesSize
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

    private fun sortContributors(dexCompressedRatio: Double, contributor: Set<Contributor>): List<Contributor> {
        val data = contributor.toList()
        Collections.sort(data, Comparator<Contributor> { o1, o2 ->
            val size1 = o1.getDownloadSize(dexCompressedRatio)
            val size2 = o2.getDownloadSize(dexCompressedRatio)
            if (size1 > size2) -1
            else if (size1 < size2) 1
            else 0
        })
        return data
    }
}

