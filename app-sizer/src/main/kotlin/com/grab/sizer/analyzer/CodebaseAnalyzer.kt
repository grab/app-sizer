package com.grab.sizer.analyzer

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.*
import javax.inject.Inject


/**
 * A specific implementation of the Analyzer interface with a focus on project codebase analysis.
 * Assigned to handle [com.grab.sizer.AnalyticsOption.CODEBASE], this class provides a detailed report on the
 * size contributions of individual team to the total app download size.
 *
 * @property dataParser Handles the parsing of APK, AAR, or JAR files.
 * @property apkComponentProcessor Processes APK, AAR, or JAR files to produce a list of contributors.
 * @property teamMapping Maps module to their corresponding team and vise versa
 * @property reportWriters A set of writers for generating and handling report output.
 * @property projectInfoProvider Provides information related to the current project.
 */
internal class CodebaseAnalyzer @Inject constructor(
    private val dataParser: DataParser,
    private val apkComponentProcessor: ApkComponentProcessor,
    private val teamMapping: TeamMapping,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider
) : Analyzer {
    override fun process() {
        /**
         * Process the whole project to get the app module information
         */
        val wholeProject = apkComponentProcessor
            .process(
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
            //others = wholeProject.noOwnerOthers.castToRawFile()
        )

        val modulesData = apkComponentProcessor
            .process(
                dataParser.apks,
                dataParser.moduleAars,
                dataParser.moduleJars
            )
        report(dataParser.apks, modulesData.contributors + appModule)
    }

    private fun report(androidBinaryInfo: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        reportTeam(androidBinaryInfo, contributors.toTeams(teamMapping))
    }

    private fun reportTeam(apks: Set<ApkFileInfo>, teams: List<Team>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val sortedTeamsReport = teams.sort(dexCompressedRatio)
            .map { it.toReportRow(dexCompressedRatio) }
        reportWriters.forEach {
            it.write(
                AnalyticsOption.CODEBASE.name.toLowerCase(),
                Report(
                    id = METRICS_ID_CODEBASE,
                    name = METRICS_ID_CODEBASE,
                    rows = sortedTeamsReport,
                    projectInfo = projectInfoProvider.getProjectInfo(),
                    customProperties = projectInfoProvider.getCustomProperties()
                )
            )
        }
    }

    private fun Team.toReportRow(dexCompressedRatio: Double): Row = createRow(
        name,
        getDownloadSize(dexCompressedRatio),
    )
}
