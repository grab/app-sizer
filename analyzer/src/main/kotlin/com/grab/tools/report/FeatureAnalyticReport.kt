package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.di.NAMED_DEVICE_NAME
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class FeatureAnalyticReport @Inject constructor(
    private val featureMapping: FeatureMapping,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    @Named(NAMED_DEVICE_NAME)
    private val deviceName: String?
) : AnalyticReport {
    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        reportFeatures(androidBinaryInfo, buildFeatures(contributors))
    }

    private fun buildFeatures(contributor: Set<Contributor>): List<Feature> {
        val moduleToContributorMap = contributor.moduleToContributors().toMutableMap()
        val featureToContributorMap = featureMapping.featureToModuleMap.mapValues { entry ->
            entry.value.flatMap { module ->
                if (moduleToContributorMap[module] == null) println("Can not find: $module")
                moduleToContributorMap.remove(module) ?: emptyList()
            }
        } + moduleToContributorMap
        val features = featureToContributorMap.map {
            Feature(it.key, it.value)
        }
        return features
    }

    private fun reportFeatures(apks: Set<ApkFileInfo>, features: List<Feature>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val sortedFeaturesReport = sortFeatures(dexCompressedRatio, features)
            .map { it.toReportItem(dexCompressedRatio) }
        reportWriters.forEach {
            it.write(
                apks.toAppInfo(deviceName),
                listOf(apkReport) + sortedFeaturesReport,
                METRICS_ID_FEATURES
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

    private fun Feature.toReportItem(dexCompressedRatio: Double): ReportItem = ReportItem(
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

    private fun totalLibsContributor(dexCompressedRatio: Double, data: List<Feature>): ReportItem =
        data.reduce { pre, cur ->
            pre.copy(
                name = "All features",
                contributors = pre.contributors + cur.contributors
            )
        }.toReportItem(dexCompressedRatio)
            .copy(
                id = "all_features",
                extraInfo = "Sum up all features values"
            )

    private fun sortFeatures(dexCompressedRatio: Double, contributor: List<Feature>): List<Feature> {
        Collections.sort(contributor, Comparator<Feature> { o1, o2 ->
            val size1 = o1.getDownloadSize(dexCompressedRatio)
            val size2 = o2.getDownloadSize(dexCompressedRatio)
            if (size1 > size2) -1
            else if (size1 < size2) 1
            else 0
        })
        return contributor
    }
}

internal fun Set<Contributor>.moduleToContributors(): Map<String, List<Contributor>> {
    return asSequence()
        .map { it.path to it }
        .map {
            val segments = it.first.removeRange(it.first.indexOf("/build/"), it.first.length).split("/")
            val moduleName = segments[segments.size - 1]
            moduleName to it.second
        }
        .groupBy { it.first }
        .mapValues { item ->
            item.value.map { it.second }
        }
}


private data class Feature(
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