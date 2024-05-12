package com.grab.sizer.analyzer

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.analyzer.model.castToClass
import com.grab.sizer.analyzer.model.castToRawFile
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.*
import javax.inject.Inject


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
                reportLargeFiles(features)
            }

    }

    private fun Set<Contributor>.filterLargeFileContributors(): Set<Contributor> = map {
        val resources = it.resources.filter { file -> file.size >= SIZE_THRESHOLD }.toSet()
        val assets = it.assets.filter { file -> file.size >= SIZE_THRESHOLD }.toSet()
        return@map it.copy(resources = resources, assets = assets)
    }.filter { it.resources.isNotEmpty() || it.assets.isNotEmpty() }
        .toSet()

    private fun reportLargeFiles(features: List<Feature>) {
        val sortedFeaturesReport = features.sorByResources()
        val reportRows = sortedFeaturesReport.toReportRows()

        reportWriters.forEach {
            it.write(
                AnalyticsOption.LARGE_FILE.name.toLowerCase(),
                Report(
                    id = METRICS_ID_LARGE_FILES,
                    name = METRICS_ID_LARGE_FILES,
                    rows = reportRows,
                    projectInfo = projectInfoProvider.getProjectInfo(),
                    customProperties = projectInfoProvider.getCustomProperties()
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
                                TagField(
                                    name = "owner",
                                    value = pair.first.name
                                ),
                                TagField(
                                    name = "module",
                                    value = module.name
                                )
                            ) + createContributorFields(
                                name = fileName,
                                value = res.downloadSize,
                            )
                        )
                    }
            }

        }
}