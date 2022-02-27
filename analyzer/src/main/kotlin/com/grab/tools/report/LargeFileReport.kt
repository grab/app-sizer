package com.grab.tools.report

import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.di.NAMED_DEVICE_NAME
import com.grab.tools.model.Contributor
import com.grab.tools.utils.ModuleUtils
import javax.inject.Inject
import javax.inject.Named


class LargeFileReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val featureMapping: FeatureMapping,
    @Named(NAMED_DEVICE_NAME)
    private val deviceName: String?
) : AnalyticReport {

    companion object {
        private const val LIBRARIES_ID = "Libraries"
        private const val NON_TRACKING_ID = "Others"
        private const val SIZE_THRESHOLD = 10 * 1024
    }

    override fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val filteredContributors = filterLargeFiles(contributors)
        val modules = ModuleUtils.mapContributorToModule(filteredContributors)
        reportFeatures(apks, modules)
    }

    private fun filterLargeFiles(contributors: Set<Contributor>): Set<Contributor> {
        return contributors
            .map {
                val resources = it.resources
                    .filter { file ->
                        file.size >= SIZE_THRESHOLD
                    }.toSet()
                val assets = it.assets.filter { file ->
                    file.size >= SIZE_THRESHOLD
                }.toSet()
                return@map it.copy(resources = resources, assets = assets)
            }.toSet()
    }

    private fun reportFeatures(apks: Set<ApkFileInfo>, modules: List<Module>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val sortedFeaturesReport = sortFeatures(dexCompressedRatio, modules)
            .flatMap { module ->
                toReportItem(module, featureMapping.moduleToFeatureMap)
            }
            .sortedBy {
                it.owner
            }
        val totalModuleReport = totalModuleReport(sortedFeaturesReport)
        val librariesReport = librariesReport(apkReport, totalModuleReport)
        featureMapping.moduleToFeatureMap
        reportWriters.forEach {
            it.write(
                apks.toAppInfo(deviceName),
                listOf(apkReport, librariesReport) + sortedFeaturesReport,
                METRICS_ID_MODULES
            )
        }
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

    private fun sortFeatures(dexCompressedRatio: Double, contributor: List<Module>): List<Module> {
        val sorted = contributor.sortedWith { o1, o2 ->
            val size1 = o1.getDownloadSize(dexCompressedRatio)
            val size2 = o2.getDownloadSize(dexCompressedRatio)
            if (size1 > size2) -1
            else if (size1 < size2) 1
            else 0
        }
        return sorted
    }

    private fun toReportItem(module: Module, moduleToFeatureMap: Map<String, String>): List<ReportItem> {
        val name = module.name
        val resourceReportItems = module.contributors.flatMap { contributor ->
            contributor.resources
                .map { res ->
                    val segmentPaths = res.path.split("/")
                    val fileName = segmentPaths.last()
                    ReportItem(
                        name = fileName,
                        id = res.path,
                        owner = moduleToFeatureMap[name],
                        extraInfo = "This file is in module $name",
                        totalDownloadSize = res.size
                    )
                }
        }
        val assetReportItems = module.contributors.flatMap { contributor ->
            contributor.assets
                .map { res ->
                    val segmentPaths = res.path.split("/")
                    val fileName = segmentPaths.last()
                    ReportItem(
                        name = fileName,
                        id = res.path,
                        owner = moduleToFeatureMap[name],
                        extraInfo = "This file is in module $name",
                        totalDownloadSize = res.size
                    )
                }
        }
        return resourceReportItems + assetReportItems
    }

}