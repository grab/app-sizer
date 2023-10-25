package com.grab.tools.analyzer

import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.model.Contributor
import com.grab.tools.model.castToClass
import com.grab.tools.model.castToRawFile
import com.grab.tools.report.LargeFileReport
import javax.inject.Inject

class LargeFileAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val analyticReport: LargeFileReport,
    private val dataParser: DataParser
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

        analyticReport.report(dataParser.apks, processedData.contributors + appModule)
    }
}