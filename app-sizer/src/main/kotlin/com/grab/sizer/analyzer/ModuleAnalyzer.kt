/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.*
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.getAars
import com.grab.sizer.parser.getJars
import com.grab.sizer.report.Report
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
            originalOwner = createAppInfo(),
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
        return generateReport(processedData.contributors + appModule)
    }

    private fun generateReport(contributors: Set<Contributor>): Report = contributors.toModules()
        .run {
            val sortedTeamsReport = sortedBy { it.getDownloadSize() }
                .map { it.toReportItem(teamMapping.moduleToTeamMap) }
            return Report(
                id = METRICS_ID_MODULES,
                name = METRICS_ID_MODULES,
                rows = toReportRows(sortedTeamsReport),
            )
        }

    private fun toReportRows(reportItems: List<ReportItem>) =
        reportItems.map { reportItem ->
            createRow(
                name = reportItem.id,
                value = reportItem.totalDownloadSize,
                owner = reportItem.owner ?: NOT_AVAILABLE_VALUE,
                rowName = reportItem.name
            )
        }
}