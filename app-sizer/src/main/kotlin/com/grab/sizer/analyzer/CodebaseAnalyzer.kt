package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.Report
import com.grab.sizer.report.Row
import javax.inject.Inject


/**
 * A specific implementation of the Analyzer interface with a focus on project codebase analysis.
 * Assigned to handle [com.grab.sizer.AnalyticsOption.CODEBASE], this class provides a detailed report on the
 * size contributions of individual team to the total app download size.
 *
 * @property dataParser Handles the parsing of APK, AAR, or JAR files.
 * @property apkComponentProcessor Processes APK, AAR, or JAR files to produce a list of contributors.
 * @property teamMapping Maps module to their corresponding team and vise versa
 */
internal class CodebaseAnalyzer @Inject constructor(
    private val dataParser: DataParser,
    private val apkComponentProcessor: ApkComponentProcessor,
    private val teamMapping: TeamMapping,
) : Analyzer {
    override fun process(): Report {
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
        return generateReport(modulesData.contributors + appModule)
    }

    private fun generateReport(contributors: Set<Contributor>): Report {
        val teams: List<Team> = contributors.toTeams(teamMapping)
        val sortedTeamsReport = teams.sort()
            .map { it.toReportRow() }
        return Report(
            id = METRICS_ID_CODEBASE,
            name = METRICS_ID_CODEBASE,
            rows = sortedTeamsReport
        )
    }

    private fun Team.toReportRow(): Row = createRow(
        name,
        getDownloadSize(),
    )
}
