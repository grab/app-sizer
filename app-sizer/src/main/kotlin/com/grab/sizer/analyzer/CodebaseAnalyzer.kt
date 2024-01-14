package com.grab.sizer.analyzer

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.analyzer.model.castToClass
import com.grab.sizer.analyzer.model.castToRawFile
import com.grab.sizer.analyzer.model.sort
import com.grab.sizer.analyzer.model.toFeatures
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.*
import javax.inject.Inject

internal const val METRICS_ID_FEATURES = "mobile.pax.app.sizex.tfs5"
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

    private fun reportFeatures(apks: Set<ApkFileInfo>, features: List<com.grab.sizer.analyzer.model.Feature>) {
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
                    projectInfo = projectInfoProvider.getProjectInfo(),
                    customProperties = projectInfoProvider.getCustomProperties()
                )
            )
        }
    }

    private fun com.grab.sizer.analyzer.model.Feature.toReportRow(dexCompressedRatio: Double): Row = createRow(
        name,
        getDownloadSize(dexCompressedRatio),
    )
}
