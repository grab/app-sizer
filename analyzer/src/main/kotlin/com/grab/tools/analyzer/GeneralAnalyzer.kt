package com.grab.tools.analyzer

import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.model.Contributor
import com.grab.tools.model.castToClass
import com.grab.tools.model.castToRawFile
import com.grab.tools.report.GeneralAnalyticReport
import javax.inject.Inject

interface Analyzer {
    fun process()
}

class GeneralAnalyzer @Inject constructor(
    private val dataParser: DataParser,
    private val apkComponentProcessor: ApkComponentProcessor,
    private val generalAnalyticReport: GeneralAnalyticReport
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
        generalAnalyticReport.report(dataParser.apks, modulesData.contributors + appModule)
    }
}