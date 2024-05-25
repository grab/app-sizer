package com.grab.sizer.analyzer

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.*
import com.grab.sizer.utils.InputProvider
import javax.inject.Inject

/**
 * A specific implementation of the Analyzer interface with a focus on identifying large files in the project.
 * This class handles [com.grab.sizer.AnalyticsOption.LARGE_FILE] and generates a report listing large files
 * along with their corresponding modules and owners.
 * Files are considered 'large' if their download size exceeds a user-configurable threshold.
 *
 * @property apkComponentProcessor Responsible for processing APK, AAR, or JAR files to compile a list of contributors.
 * @property dataParser Parse APK, AAR, or JAR files.
 * @property reportWriters A set of report writers
 * @property teamMapping Handles the bi-directional mapping between modules and teams.
 * @property projectInfoProvider Provide project-related information.
 * @property inputProvider Provides the input used for analysis, such as threshold value for large file identification.
 */
internal class LargeFileAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val teamMapping: TeamMapping,
    private val projectInfoProvider: ProjectInfoProvider,
    private val inputProvider: InputProvider
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
            .toTeams(teamMapping).also { teams ->
                reportLargeFiles(teams)
            }

    }

    private fun Set<Contributor>.filterLargeFileContributors(): Set<Contributor> = map {
        val resources = it.resources.filter { file -> file.size >= inputProvider.provideLargeFileThreshold() }.toSet()
        val assets = it.assets.filter { file -> file.size >= inputProvider.provideLargeFileThreshold() }.toSet()
        return@map it.copy(resources = resources, assets = assets)
    }.filter { it.resources.isNotEmpty() || it.assets.isNotEmpty() }
        .toSet()

    private fun reportLargeFiles(teams: List<Team>) {
        val sortedTeamsReport = teams.sorByResources()
        val reportRows = sortedTeamsReport.toReportRows()

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

    private fun List<Team>.sorByResources(): List<Team> = this.sortedBy {
        it.resourcesDownloadSize + it.assetsDownloadSize
    }

    private fun List<Team>.toReportRows(): List<Row> = map { it to it.modules }
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