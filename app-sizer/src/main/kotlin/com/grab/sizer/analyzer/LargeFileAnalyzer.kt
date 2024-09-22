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
import com.grab.sizer.report.Row
import javax.inject.Inject
import javax.inject.Named

/**
 * A specific implementation of the Analyzer interface with a focus on identifying large files in the project.
 * This class handles [com.grab.sizer.AnalyticsOption.LARGE_FILE] and generates a report listing large files
 * along with their corresponding modules and owners. (Haven't supported library)
 * Files are considered 'large' if their download size exceeds a user-configurable threshold.
 *
 * @property apkComponentProcessor Responsible for processing APK, AAR, or JAR files to compile a list of contributors.
 * @property dataParser Parse APK, AAR, or JAR files.
 * @property teamMapping Handles the bi-directional mapping between modules and teams.
 * @property largeFileThreshold threshold value for large file identification.
 */
internal class LargeFileAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val dataParser: DataParser,
    private val teamMapping: TeamMapping,
    @Named("largeFileThreshold")
    private val largeFileThreshold: Long
) : Analyzer {
    override fun process(): Report {
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
            originalOwner = createAppInfo(),
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

        return generateReport(processedData.contributors + appModule)
    }

    private fun generateReport(contributors: Set<Contributor>): Report {
        return contributors.filterLargeFileContributors()
            .toTeams(teamMapping)
            .run {
                reportLargeFiles(this)
            }

    }

    private fun Set<Contributor>.filterLargeFileContributors(): Set<Contributor> = map {
        val resources = it.resources.filter { file -> file.downloadSize >= largeFileThreshold }.toSet()
        val assets = it.assets.filter { file -> file.downloadSize >= largeFileThreshold }.toSet()
        return@map it.copy(resources = resources, assets = assets)
    }.filter { it.resources.isNotEmpty() || it.assets.isNotEmpty() }
        .toSet()

    private fun reportLargeFiles(teams: List<Team>): Report {
        val sortedTeamsReport = teams.sorByResources()
        val reportRows = sortedTeamsReport.toReportRows()
        return Report(
            id = METRICS_ID_LARGE_FILES,
            name = METRICS_ID_LARGE_FILES,
            rows = reportRows,
        )
    }

    private fun List<Team>.sorByResources(): List<Team> = this.sortedBy {
        it.resourcesDownloadSize + it.assetsDownloadSize
    }

    private fun List<Team>.toReportRows(): List<Row> = map { it to it.modules }
        .flatMap { pair ->
            pair.second.flatMap { module ->
                module.contributors.flatMap { contributor -> contributor.resources + contributor.assets }
                    .map { res ->
                        val segmentPaths = res.path.split("/")
                        val fileName = segmentPaths.last()
                        createRow(
                            name = fileName,
                            value = res.downloadSize,
                            owner = pair.first.name,
                            tag = module.tag,
                            rowName = fileName
                        )
                    }
            }
        }
}