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
import com.grab.sizer.parser.BinaryFileInfo
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

        val appContributor = Contributor(
            originalOwner = createAppInfo(),
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
        return generateReport(modulesData.contributors + appContributor)
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

internal fun createAppInfo() = object : BinaryFileInfo {
    override val name: String = "app"
    override val path: String = "root/app/build/"
    override val tag: String = "app"
}