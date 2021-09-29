package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.log.log
import java.util.*


class FeatureAnalyticReport(
    private val featureMapping: FeatureMapping,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val deviceName: String?
) : AnalyticReport {
    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>) {
        reportFeatures(androidBinaryInfo, buildFeatures(contributor))
    }

    private fun buildFeatures(contributor: Set<Contributor>): List<Feature> {
        val moduleToContributorMap = contributor.moduleToContributor().toMutableMap()
        val featureToContributorMap = featureMapping.featureToModuleMap.mapValues { entry ->
            entry.value.flatMap { module ->
                if (moduleToContributorMap[module] == null) log("Can not find: $module")
                moduleToContributorMap[module] ?: emptyList()
            }
        }
        val features = featureToContributorMap.map {
            Feature(it.key, it.value)
        }
        return features
    }

    private fun reportFeatures(apks: Set<ApkFileInfo>, features: List<Feature>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val apkReport = apks.apksSizeReport(dexCompressedRatio)
        val totalFeaturesReport = totalLibsContributor(dexCompressedRatio, features)
        val sortedFeaturesReport = sortFeatures(dexCompressedRatio, features)
            .map { it.toReportItem(dexCompressedRatio) }
        reportWriters.forEach {
            it.write(apks.toAppInfo(deviceName), listOf(apkReport, totalFeaturesReport) + sortedFeaturesReport)
        }
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

    private fun Set<Contributor>.moduleToContributor(): Map<String, List<Contributor>> {
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
}

internal fun Set<ApkFileInfo>.dexDownloadRatio(): Double {
    val dexDownloadSize = flatMap { it.dexes }.sumOf { it.downloadSize }
    val dexClassesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
    return dexDownloadSize.toDouble() / dexClassesSize
}
