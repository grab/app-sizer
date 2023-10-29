package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.mapper.ApkComponentProcessor
import com.grab.tools.analyzer.report.*
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.analyzer.model.Contributor
import com.grab.tools.parser.DataParser
import java.io.File
import javax.inject.Inject

internal const val METRICS_ID_APK = "mobile.pax.app.size.app5"

internal class ApkAnalyzer @Inject constructor(
    private val apkComponentAnalytic: ApkComponentProcessor,
    private val dataParser: DataParser,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider
) : Analyzer {
    override fun process() {
        val processedData = apkComponentAnalytic.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        report(dataParser.apks, processedData.contributors)
    }

    private fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val contributorList = contributors.sortedBy { it.getDownloadSize(dexCompressedRatio) }
        val apkReportRow = createApkReportRow(apks, dexCompressedRatio)
        val totalLibsReport = totalLibrariesReport(dexCompressedRatio, contributorList)
        val libComponentReport = libComponentReport(totalLibsReport)
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val codeBaseReports = codeBaseComponentReport(codeBaseReport(totalLibsReport, apkReport))
        val listOfReport = listOf(apkReportRow) + codeBaseReports + libComponentReport

        reportWriters.forEach {
            it.write(
                AnalyticsOption.APK.name.toLowerCase(),
                Report(
                    projectInfo = projectInfoProvider.get(),
                    rows = listOfReport,
                    id = METRICS_ID_APK,
                    name = METRICS_ID_APK
                )
            )
        }
    }

    private fun createApkReportRow(
        apks: Set<ApkFileInfo>,
        dexCompressedRatio: Double
    ) = Row(
        fields = listOf(
            apks.toReportField(dexCompressedRatio)
        ),
        name = "Apk"
    )

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
        assetDownloadSize = apkReport.assetDownloadSize - totalLibsReport.assetDownloadSize,
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


    private fun libComponentReport(allLibReport: ReportItem): List<Row> = listOf(
        createRow(
            name = "android-java-libraries",
            value = allLibReport.totalDownloadSize - allLibReport.nativeLibDownloadSize
        ),
        createRow(
            name = "native-libraries",
            value = allLibReport.totalDownloadSize - allLibReport.nativeLibDownloadSize
        )
    )

    private fun codeBaseComponentReport(codeBaseReport: ReportItem): List<Row> = listOf(
        createRow(
            name = "codebase-kotlin-java",
            value = codeBaseReport.classesDownloadSize,
        ),
        createRow(
            name = "codebase-resources",
            value = codeBaseReport.resourceDownloadSize,
        ),
        createRow(
            name = "codebase-assets",
            value = codeBaseReport.assetDownloadSize,
        ),
        createRow(
            name = "codebase-native",
            value = codeBaseReport.nativeLibDownloadSize,
        ),
        createRow(
            name = "others",
            value = codeBaseReport.otherDownloadSize,
        ),
    )

    private fun createRow(name: String, value: Long): Row = Row(
        fields = listOf(
            HybridField(
                name = name,
                value = value
            )
        ),
        name = name
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
}