package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.Report
import com.grab.sizer.report.dexDownloadRatio
import javax.inject.Inject


/**
 * An implementation of the Analyzer interface, focused on analyzing all modules within the project.
 * This class is designed to handle [com.grab.sizer.AnalyticsOption.MODULES].
 * The resulting report lists all module in the project along with their respective contributions to the total app download size.
 * The list of modules could be grouped by the owner.
 *
 * @property apkComponentProcessor An instance for processing APK, AAR, or JAR files to produce a list of contributors.
 * @property dataParser Parses APK, AAR, and JAR files for analysis.
 * @property teamMapping Maps module to their corresponding team and vise versa
 */
internal class ModuleAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser,
    private val teamMapping: TeamMapping,
) : Analyzer {
    override fun process(): Report {
        /**
         * Process the whole project to get the app module information
         */
        val wholeProject = apkComponentProcessor.process(
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
            others = wholeProject.noOwnerOthers.castToRawFile()
        )

        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.moduleAars,
            dataParser.moduleJars
        )
        return generateReport(dataParser.apks, processedData.contributors + appModule)
    }

    private fun generateReport(apks: Set<ApkFileInfo>, contributors: Set<Contributor>): Report {
        contributors.toModules()
            .run {
                val dexCompressedRatio = apks.dexDownloadRatio()
                val sortedTeamsReport = sortedBy { it.getDownloadSize(dexCompressedRatio) }
                    .map { it.toReportItem(dexCompressedRatio, teamMapping.moduleToTeamMap) }
                return Report(
                    id = METRICS_ID_MODULES,
                    name = METRICS_ID_MODULES,
                    rows = toReportRows(sortedTeamsReport),
                )
            }
    }

    private fun toReportRows(reportItems: List<ReportItem>) =
        reportItems.map { reportItem ->
            createRow(
                name = reportItem.id,
                value = reportItem.totalDownloadSize,
                owner = reportItem.owner ?: "",
                rowName = reportItem.name
            )
        }
}

internal fun Set<Contributor>.toModules(): List<Module> = moduleToContributors().map {
    Module(
        it.key,
        it.value
    )
}