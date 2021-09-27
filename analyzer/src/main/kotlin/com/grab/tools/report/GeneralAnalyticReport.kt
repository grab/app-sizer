package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.apk.ApkFileInfo
import java.util.*


class GeneralAnalyticReport(
    private val featureMapping: FeatureMapping,
    private val featureReportWriters: Set<@JvmSuppressWildcards FeatureReportWriter>
) : AnalyticReport {
    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>) {
        val moduleToContributorMap = contributor.moduleToContributor().toMutableMap()
        val featureToContributorMap = featureMapping.featureToModuleMap.mapValues { entry ->
            entry.value.flatMap { module ->
                if (moduleToContributorMap[module] == null) println("Can not find: $module")
                moduleToContributorMap.remove(module) ?: emptyList()
            }
        }
        val features = featureToContributorMap.map {
            Feature(it.key, it.value)
        } + moduleToContributorMap.map { // module with no feature
            Feature(it.key, it.value)
        }

        report(androidBinaryInfo, features)
    }

    private fun report(apks: Set<ApkFileInfo>, features: List<Feature>) {
        featureReportWriters.forEach { it.initTile() }
        val dexCompressedRatio = dexDownloadRatio(apks)
        reportApkSize(dexCompressedRatio, apks)
        val data = sortData(dexCompressedRatio, features)
        totalLibsContributor(dexCompressedRatio, data)
        featureReportWriters.forEach {
            it.reportEachFeature(dexCompressedRatio, data)
        }
    }

    private fun dexDownloadRatio(apks: Set<ApkFileInfo>): Double {
        val dexDownloadSize = apks.flatMap { it.dexes }.sumOf { it.downloadSize }
        val dexClassesSize = apks.flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        return dexDownloadSize.toDouble() / dexClassesSize
    }

    private fun reportApkSize(dexCompressedRatio: Double, apks: Set<ApkFileInfo>) {
        val resourceSize = apks.flatMap { it.resources }.sumOf { it.downloadSize }
        val nativeLibSize = apks.flatMap { it.nativeLibs }.sumOf { it.downloadSize }
        val assetSizes = apks.flatMap { it.assets }.sumOf { it.downloadSize }
        val othersSize = apks.flatMap { it.others }.sumOf { it.downloadSize }
        val classesSize = apks.flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
        val total = resourceSize + nativeLibSize + assetSizes + othersSize + classDownloadSize
        featureReportWriters.forEach {
            it.reportApksSize(
                resourceSize = resourceSize,
                nativeLibSize = nativeLibSize,
                assetSizes = assetSizes,
                othersSize = othersSize,
                classesSize = classesSize,
                classDownloadSize = classDownloadSize,
                total = total
            )
        }
    }

    private fun totalLibsContributor(dexCompressedRatio: Double, data: List<Feature>) {
        data.reduce { pre, cur ->
            pre.copy(
                name = "All features",
                contributors = pre.contributors + cur.contributors
            )
        }.also { allLibs ->
            featureReportWriters.forEach { it.reportTotalFeatures(dexCompressedRatio, allLibs) }
        }
    }

    private fun sortData(dexCompressedRatio: Double, contributor: List<Feature>): List<Feature> {
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

