package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.*
import java.io.File
import javax.inject.Inject

internal const val CODE_BASE_ID = "Codebase"

/**
 * A specific implementation of the Analyzer, focusing on APK analysis.
 * This class take responsibility to handle [com.grab.sizer.AnalyticsOption.APK]
 * It processes APK, AAR, and JAR components and generates detailed reports that break down the binary download size into:
 * - android-java-libraries: Represents the size contributions from jar & aar libraries (excluding native .so files).
 * - codebase-kotlin-java: Denotes the size contributions from the codebase's Java and Kotlin classes.
 * - codebase-resources: Specifies the size contributions from the codebase's resources (eg: images, layouts).
 * - codebase-assets: Represents the size contributions from the codebase's asset files.
 * - codebase-native: Symbolizes the size contributions from the codebase's native C/C++ libraries.
 * - native-libraries: Indicates the size contributions from native libraries (C/C++).
 *
 * @property apkComponentProcessor Responsible for processing APK, AAR or JAR files to generate the contributors
 * @property dataParser to parse APK, AAR or JAR files.
 */
internal class ApkAnalyzer @Inject constructor(
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
        val apkReportRow = createApkReportRow(apks, dexCompressedRatio)
        val totalLibsReport = totalLibrariesReport(dexCompressedRatio, contributorList)
        val libComponentReport = libComponentReport(totalLibsReport)
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val codeBaseReports = codeBaseComponentReport(codeBaseReport(totalLibsReport, apkReport))
        val listOfReport = listOf(apkReportRow) + codeBaseReports + libComponentReport

        return Report(
            rows = listOfReport,
            id = METRICS_ID_APK,
            name = METRICS_ID_APK,
        )
    }

    private fun createApkReportRow(
        apks: Set<ApkFileInfo>,
        dexCompressedRatio: Double
    ) = Row(
        fields = apks.toReportField(dexCompressedRatio),
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
            value = allLibReport.nativeLibDownloadSize
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