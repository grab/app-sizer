package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.di.NAMED_DEVICE_NAME
import java.util.*
import javax.inject.Inject
import javax.inject.Named

private const val LIBRARIES_ID = "Libraries"
internal const val NON_TRACKING_ID = "Others"

class ModuleAnalyticReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    @Named(NAMED_DEVICE_NAME)
    private val deviceName: String?
) : AnalyticReport {
    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>) {
        reportFeatures(androidBinaryInfo, buildFeatures(contributor))
    }

    private fun reportFeatures(apks: Set<ApkFileInfo>, modules: List<Module>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val sortedFeaturesReport = sortFeatures(dexCompressedRatio, modules)
            .map { it.toReportItem(dexCompressedRatio) }
        val totalModuleReport = totalModuleReport(sortedFeaturesReport)
        val librariesReport = librariesReport(apkReport, totalModuleReport)

        reportWriters.forEach {
            it.write(
                apks.toAppInfo(deviceName),
                listOf(apkReport, librariesReport, otherReport(apkReport)) + sortedFeaturesReport,
                MODULES_METRICS_ID
            )
        }
    }

    private fun otherReport(apkReport: ReportItem): ReportItem {
        return ReportItem(
            id = NON_TRACKING_ID,
            name = NON_TRACKING_ID,
            totalDownloadSize = apkReport.otherDownloadSize
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

    private fun buildFeatures(contributor: Set<Contributor>): List<Module> {
        val moduleToContributorMap = contributor.moduleToContributors().toMutableMap()
        val features = moduleToContributorMap.map {
            Module(it.key, it.value)
        }
        return features
    }

    private fun Module.toReportItem(dexCompressedRatio: Double): ReportItem = ReportItem(
        name = name,
        id = name,
        extraInfo = "Sum up all codebase for $name",
        totalDownloadSize = getDownloadSize(dexCompressedRatio),
        classesSize = classSize,
        classesDownloadSize = getClassDownloadSize(dexCompressedRatio),
        nativeLibDownloadSize = nativeLibDownloadSize,
        resourceDownloadSize = resourcesDownloadSize,
        assetDownloadSize = assetsDownloadSize,
        otherDownloadSize = othersDownloadSize
    )

    private fun totalLibsContributor(dexCompressedRatio: Double, data: List<Module>): ReportItem =
        data.reduce { pre, cur ->
            pre.copy(
                name = "All Module",
                contributors = pre.contributors + cur.contributors
            )
        }.toReportItem(dexCompressedRatio)
            .copy(
                id = "all_modules",
                extraInfo = "Sum up all modules values"
            )

    private fun sortFeatures(dexCompressedRatio: Double, contributor: List<Module>): List<Module> {
        Collections.sort(contributor, Comparator<Module> { o1, o2 ->
            val size1 = o1.getDownloadSize(dexCompressedRatio)
            val size2 = o2.getDownloadSize(dexCompressedRatio)
            if (size1 > size2) -1
            else if (size1 < size2) 1
            else 0
        })
        return contributor
    }
}

private data class Module(
    val name: String,
    val contributors: List<Contributor>
) {
    val resourcesDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.othersDownloadSize } }
    val classSize: Long by lazy { contributors.sumOf { contributor -> contributor.classSize } }

    fun getClassDownloadSize(downloadSizeRatio: Double): Long = (classSize * downloadSizeRatio).toLong()

    fun getDownloadSize(downloadSizeRatio: Double): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + getClassDownloadSize(
            downloadSizeRatio
        )
}

