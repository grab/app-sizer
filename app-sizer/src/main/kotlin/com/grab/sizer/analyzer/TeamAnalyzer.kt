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
 * Team-focused analyzer that provides comprehensive team dashboard analysis.
 * Assigned to handle [com.grab.sizer.AnalyticsOption.TEAMS], this class provides a detailed report on the
 * total size contributions of individual teams to the app download size, combining both module and library contributions.
 *
 * @property dataParser Handles the parsing of APK, AAR, or JAR files.
 * @property apkComponentProcessor Processes APK, AAR, or JAR files to produce a list of contributors.
 * @property teamMapping Optional team mapping for modules and libraries.
 */
internal class TeamAnalyzer @Inject constructor(
    private val dataParser: DataParser,
    private val apkComponentProcessor: ApkComponentProcessor,
    private val teamMapping: TeamMapping?,
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

        // Process all contributors together (modules + libraries) for team analysis
        val allContributorsData = apkComponentProcessor
            .process(
                dataParser.apks,
                dataParser.getAars(),
                dataParser.getJars()
            )

        return generateTeamReport(allContributorsData.contributors + appContributor)
    }

    private fun generateTeamReport(contributors: Set<Contributor>): Report {
        // Convert all contributors to teams (handles both modules and libraries)
        val teams: List<Team> = contributors.toTeams(teamMapping)

        val allTeamRows = teams.sortedBy { it.getDownloadSize() }
            .flatMap { it.toDetailedReportRows() }
        return Report(
            id = METRICS_ID_TEAM,
            name = METRICS_ID_TEAM,
            rows = allTeamRows
        )
    }

    private fun Team.toDetailedReportRows(): List<Row> {
        return teamTotalReport() + teamLibrariesReport() + teamComponentReport()
    }

    private fun Team.teamTotalReport(): List<Row> = listOf(
        createRow(
            name = TOTAL_ID,
            value = getDownloadSize(),
            owner = name,
            rowName = name
        )
    )

    private fun Team.teamLibrariesReport(): List<Row> = listOf(
        createRow(
            name = ANDROID_JAVA_LIBRARIES_ID,
            value = libTotalDownloadSize - libNativeDownloadSize,
            owner = name,
            rowName = name
        ),
        createRow(
            name = NATIVE_LIBRARIES_ID,
            value = libNativeDownloadSize,
            owner = name,
            rowName = name
        )
    )

    private fun Team.teamComponentReport(): List<Row> = listOf(
        createRow(
            name = CODEBASE_KOTLIN_JAVA_ID,
            value = codebaseClassDownloadSize,
            owner = name,
            rowName = name
        ),
        createRow(
            name = CODEBASE_RESOURCES_ID,
            value = codebaseResourcesDownloadSize,
            owner = name,
            rowName = name
        ),
        createRow(
            name = CODEBASE_ASSETS_ID,
            value = codebaseAssetsDownloadSize,
            owner = name,
            rowName = name
        ),
        createRow(
            name = CODEBASE_NATIVE_ID,
            value = codebaseNativeLibDownloadSize,
            owner = name,
            rowName = name
        ),
        createRow(
            name = OTHERS_ID,
            value = codebaseOthersDownloadSize,
            owner = name,
            rowName = name
        )
    )
}

internal fun createAppInfo() = object : BinaryFileInfo {
    override val name: String = "app"
    override val path: String = "root/app/build/"
    override val tag: String = "app"
}