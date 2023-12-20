package com.grab.tools.analyzer

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.mapper.ApkComponentProcessor
import com.grab.tools.analyzer.report.*
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.analyzer.model.Contributor
import com.grab.tools.analyzer.model.castToClass
import com.grab.tools.analyzer.model.castToRawFile
import com.grab.tools.parser.DataParser
import com.grab.tools.parser.getAars
import com.grab.tools.parser.getJars
import javax.inject.Inject

internal const val METRICS_ID_FEATURES = "mobile.pax.app.size.tfs5"
internal const val CODE_BASE_ID = "Codebase"


internal class CodebaseAnalyzer @Inject constructor(
    private val dataParser: DataParser,
    private val apkComponentProcessor: ApkComponentProcessor,
    private val featureMapping: FeatureMapping,
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
        reportFeatures(androidBinaryInfo, contributors.toFeatures(featureMapping))
    }

    private fun reportFeatures(apks: Set<ApkFileInfo>, features: List<Feature>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val sortedFeaturesReport = features.sort(dexCompressedRatio)
            .map { it.toReportRow(dexCompressedRatio) }
        reportWriters.forEach {
            it.write(
                AnalyticsOption.CODEBASE.name.toLowerCase(),
                Report(
                    id = METRICS_ID_FEATURES,
                    name = METRICS_ID_FEATURES,
                    rows = sortedFeaturesReport,
                    projectInfo = projectInfoProvider.get()
                )
            )
        }
    }

    private fun Feature.toReportRow(dexCompressedRatio: Double): Row {
        return Row(
            name = name,
            fields = listOf(
                HybridField(
                    name = name,
                    value = getDownloadSize(dexCompressedRatio),
                    tag = "Feature"
                ),
            )
        )
    }
}
