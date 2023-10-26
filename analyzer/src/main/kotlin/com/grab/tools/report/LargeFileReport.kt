package com.grab.tools.report

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.ProjectInfoProvider
import com.grab.tools.analyzer.report.*
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import javax.inject.Inject

// Todo : move to a configurable value
private const val SIZE_THRESHOLD = 10 * 1024

class LargeFileReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val featureMapping: FeatureMapping,
    private val projectInfoProvider: ProjectInfoProvider,
) : AnalyticReport {

    override fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        contributors.filterLargeFileContributors()
            .toFeatures(featureMapping).also { features ->
                reportLargeFiles(apks, features)
            }

    }

    private fun Set<Contributor>.filterLargeFileContributors(): Set<Contributor> = map {
        val resources = it.resources.filter { file -> file.size >= SIZE_THRESHOLD }.toSet()
        val assets = it.assets.filter { file -> file.size >= SIZE_THRESHOLD }.toSet()
        return@map it.copy(resources = resources, assets = assets)
    }.filter { it.resources.isNotEmpty() || it.assets.isNotEmpty() }
        .toSet()

    private fun reportLargeFiles(apks: Set<ApkFileInfo>, features: List<Feature>) {
        val sortedFeaturesReport = features.sorByResources()
        val reportRows = sortedFeaturesReport.toReportRows()

        reportWriters.forEach {
            it.write(
                AnalyticsOption.LARGE_FILE.name.toLowerCase(),
                Report(
                    projectInfoProvider.get(),
                    id = METRICS_ID_LARGE_FILES,
                    name = METRICS_ID_LARGE_FILES,
                    rows = reportRows
                )
            )
        }
    }

    private fun List<Feature>.sorByResources(): List<Feature> = this.sortedBy {
        it.resourcesDownloadSize + it.assetsDownloadSize
    }

    private fun List<Feature>.toReportRows(): List<Row> = map { it to it.modules }
        .flatMap { pair ->
            pair.second.flatMap { module ->
                module.contributors.flatMap { contributor -> contributor.resources + contributor.assets }
                    .map { res ->
                        val segmentPaths = res.path.split("/")
                        val fileName = segmentPaths.last()
                        Row(
                            name = pair.first.name,
                            fields = listOf(
                                HybridField(
                                    name = fileName,
                                    value = res.downloadSize,
                                    tag = module.name
                                ),
                                TagField(
                                    name = "owner",
                                    value = pair.first.name
                                ),
                                TagField(
                                    name = "module",
                                    value = module.name
                                )
                            )
                        )
                    }
            }

        }
}