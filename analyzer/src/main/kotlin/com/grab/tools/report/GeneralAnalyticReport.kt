package com.grab.tools.report

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.ProjectInfoProvider
import com.grab.tools.analyzer.report.*
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import javax.inject.Inject


class GeneralAnalyticReport @Inject constructor(
    private val featureMapping: FeatureMapping,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider
) : AnalyticReport {
    override fun report(androidBinaryInfo: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        reportFeatures(androidBinaryInfo, contributors.toFeatures(featureMapping))
    }

    private fun reportFeatures(apks: Set<ApkFileInfo>, features: List<Feature>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val apkReport = apks.toReportRow(dexCompressedRatio)
        val sortedFeaturesReport = features.sort(dexCompressedRatio)
            .map { it.toReportRow(dexCompressedRatio) }
        reportWriters.forEach {
            it.write(
                AnalyticsOption.GENERAL.name.toLowerCase(),
                Report(
                    id = METRICS_ID_FEATURES,
                    name = METRICS_ID_FEATURES,
                    rows = listOf(apkReport) + sortedFeaturesReport,
                    projectInfo = projectInfoProvider.get()
                )
            )
        }
    }

    private fun Set<ApkFileInfo>.toReportRow(dexCompressedRatio: Double): Row = Row(
        fields = listOf(toReportField(dexCompressedRatio)),
        name = "Apk"
    )

    private fun Feature.toReportRow(dexCompressedRatio: Double): Row {
        return Row(
            name = name,
            fields = listOf(
                HybridField(
                    name = name,
                    value = getDownloadSize(dexCompressedRatio),
                    tag = "Sum up all codebase for $name"
                ),
            )
        )
    }
}