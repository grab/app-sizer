package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.analyzer.report.*
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import com.grab.tools.model.castToClass
import com.grab.tools.model.castToRawFile
import com.grab.tools.report.Feature
import com.grab.tools.report.FeatureMapping
import com.grab.tools.report.toFeatures
import javax.inject.Inject

internal const val METRICS_ID_LARGE_FILES = "mobile.pax.app.size.files5"

// Todo : move to a configurable value
private const val SIZE_THRESHOLD = 10 * 1024

internal class LargeFileAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val featureMapping: FeatureMapping,
    private val projectInfoProvider: ProjectInfoProvider
) : Analyzer {
    override fun process() {
        /**
         * Process the whole project to get the app module information
         */
        val wholeProject =
            apkComponentProcessor.process(
                dataParser.apks,
                dataParser.getAars(),
                dataParser.getJars()
            )

        val appModule = Contributor(
            path = "root/app/build/",
            assets = wholeProject.noOwnerAssets.castToRawFile(),
            resources = wholeProject.noOwnerResources.castToRawFile(),
            nativeLibs = wholeProject.noOwnerNativeLibs.castToRawFile(),
            classes = wholeProject.noOwnerClasses.castToClass(),
        )

        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.moduleAars,
            dataParser.moduleJars
        )

        report(dataParser.apks, processedData.contributors + appModule)
    }

    private fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
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