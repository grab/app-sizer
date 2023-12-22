package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.mapper.ApkComponentProcessor
import com.grab.tools.analyzer.model.Contributor
import com.grab.tools.analyzer.model.castToClass
import com.grab.tools.analyzer.model.castToRawFile
import com.grab.tools.analyzer.report.*
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.parser.DataParser
import com.grab.tools.parser.getAars
import com.grab.tools.parser.getJars
import javax.inject.Inject

internal const val METRICS_ID_MODULES = "mobile.pax.app.size.mds5"

internal class ModuleAnalyzer @Inject constructor(
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
            //others = wholeProject.noOwnerOthers.castToRawFile()
        )

        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.moduleAars,
            dataParser.moduleJars
        )
        report(dataParser.apks, processedData.contributors + appModule)
    }

    private fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        contributors.toModules().also { modules -> report(apks, modules) }
    }

    private fun report(apks: Set<ApkFileInfo>, modules: List<Module>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val sortedFeaturesReport = modules.sortedBy { it.getDownloadSize(dexCompressedRatio) }
            .map { it.toReportItem(dexCompressedRatio, featureMapping.moduleToFeatureMap) }
        reportWriters.forEach {
            it.write(
                AnalyticsOption.MODULES.name.toLowerCase(),
                Report(
                    id = METRICS_ID_MODULES,
                    name = METRICS_ID_MODULES,
                    projectInfo = projectInfoProvider.getProjectInfo(),
                    rows = toReportRows(sortedFeaturesReport),
                    customProperties = projectInfoProvider.getCustomProperties()
                )
            )
        }
    }

    private fun toReportRows(reportItems: List<ReportItem>) =
        reportItems.map { reportItem ->
            Row(
                name = reportItem.name,
                fields = listOf(
                    HybridField(
                        name = reportItem.id,
                        value = reportItem.totalDownloadSize,
                        tag = reportItem.extraInfo
                    ),
                    TagField(
                        name = "owner",
                        value = reportItem.owner ?: ""
                    )
                )
            )
        }
}

internal fun Set<Contributor>.toModules(): List<Module> = moduleToContributors().map { Module(it.key, it.value) }