package com.grab.tools.report

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.METRICS_ID_MODULES
import com.grab.tools.analyzer.ProjectInfoProvider
import com.grab.tools.analyzer.report.*
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import javax.inject.Inject

internal const val LIBRARIES_ID = "Libraries"

class ModuleAnalyticReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val featureMapping: FeatureMapping,
    private val projectInfoProvider: ProjectInfoProvider
) : AnalyticReport {
    override fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        contributors.toModules().also { modules -> report(apks, modules) }
    }

    private fun report(apks: Set<ApkFileInfo>, modules: List<Module>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val sortedFeaturesReport = modules.sortedBy { it.getDownloadSize(dexCompressedRatio) }
            .map { it.toReportItem(dexCompressedRatio, featureMapping.moduleToFeatureMap) }
        val totalModuleReport = totalModuleReport(sortedFeaturesReport)
        val librariesReport = librariesReport(apkReport, totalModuleReport)
        val reportItems = listOf(apkReport, librariesReport) + sortedFeaturesReport
        reportWriters.forEach {
            it.write(
                AnalyticsOption.MODULES.name.toLowerCase(),
                Report(
                    id = METRICS_ID_MODULES,
                    name = METRICS_ID_MODULES,
                    projectInfo = projectInfoProvider.get(),
                    rows = toReportRows(reportItems)
                )
            )
        }
    }

    private fun toReportRows(reportItems: List<ReportItem>) =
        reportItems.map { reportItem ->
            Row(
                name = reportItem.name,
                fields = listOf(
                    HybridField(
                        name = reportItem.id,
                        value = reportItem.totalDownloadSize,
                        tag = reportItem.extraInfo
                    ),
                    TagField(
                        name = "owner",
                        value = reportItem.owner ?: ""
                    )
                )
            )
        }

    private fun totalModuleReport(data: List<ReportItem>): ReportItem {
        return data.reduce { pre, cur ->
            pre.copy(
                totalDownloadSize = pre.totalDownloadSize + cur.totalDownloadSize,
                resourceDownloadSize = pre.resourceDownloadSize + cur.resourceDownloadSize,
                nativeLibDownloadSize = pre.nativeLibDownloadSize + cur.nativeLibDownloadSize,
                classesSize = pre.classesSize + cur.classesSize,
                classesDownloadSize = pre.classesDownloadSize + cur.classesDownloadSize
            )
        }.copy(
            name = "All modules",
            id = "all_modules"
        )
    }

    private fun librariesReport(
        apkReport: ReportItem,
        totalModuleReport: ReportItem
    ): ReportItem = ReportItem(
        id = LIBRARIES_ID,
        name = LIBRARIES_ID,
        totalDownloadSize = apkReport.totalDownloadSize - totalModuleReport.totalDownloadSize - apkReport.otherDownloadSize,
        otherDownloadSize = apkReport.otherDownloadSize - totalModuleReport.otherDownloadSize,
        resourceDownloadSize = apkReport.resourceDownloadSize - totalModuleReport.resourceDownloadSize,
        nativeLibDownloadSize = apkReport.nativeLibDownloadSize - totalModuleReport.nativeLibDownloadSize,
        classesDownloadSize = apkReport.classesDownloadSize - totalModuleReport.classesDownloadSize,
        classesSize = apkReport.classesSize - totalModuleReport.classesSize
    )
}

internal fun Set<Contributor>.toModules(): List<Module> = moduleToContributors().map { Module(it.key, it.value) }



