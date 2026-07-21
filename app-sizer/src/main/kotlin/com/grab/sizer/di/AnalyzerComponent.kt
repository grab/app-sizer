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

package com.grab.sizer.di

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.Analyzer
import com.grab.sizer.analyzer.ApkAnalyzer
import com.grab.sizer.analyzer.BasicApkAnalyzer
import com.grab.sizer.analyzer.LargeFileAnalyzer
import com.grab.sizer.analyzer.LibContentAnalyzer
import com.grab.sizer.analyzer.LibrariesAnalyzer
import com.grab.sizer.analyzer.ModuleAnalyzer
import com.grab.sizer.analyzer.TeamAnalyzer
import com.grab.sizer.analyzer.TeamMapping
import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.mapper.AssetComponentMapper
import com.grab.sizer.analyzer.mapper.ClassComponentMapper
import com.grab.sizer.analyzer.mapper.DefaultApkComponentProcessor
import com.grab.sizer.analyzer.mapper.NativeLibComponentMapper
import com.grab.sizer.analyzer.mapper.OtherComponentMapper
import com.grab.sizer.analyzer.mapper.ResourceComponentMapper
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.DefaultAarFileParser
import com.grab.sizer.parser.DefaultApkFileParser
import com.grab.sizer.parser.DefaultDataParser
import com.grab.sizer.parser.DefaultDexFileParser
import com.grab.sizer.parser.DefaultJarFileParser
import com.grab.sizer.parser.DefaultJarStreamParser
import com.grab.sizer.parser.DefaultProguardFileParser
import com.grab.sizer.report.DatabaseReportWriter
import com.grab.sizer.report.MarkdownReportWriter
import com.grab.sizer.report.ReportWriter
import com.grab.sizer.report.db.DbReportDaoFactory
import com.grab.sizer.report.json.JsonReportWriter
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider

typealias AnalyzerClass = Class<*>

/**
 * Hand-wired object graph for a single analysis run, replacing the previous Dagger component.
 *
 * Dagger was removed from app-sizer because the library runs on the Gradle buildscript
 * classpath, where a Dagger version brought by any other build tool can clash with the
 * generated code at runtime (e.g. NoSuchMethodError: Preconditions.checkNotNullFromProvides).
 *
 * Every instance is created once per component, which preserves the semantics of the
 * previous @AppScope component: parsers are shared across analyzers, so parsed data is
 * reused within a run.
 */
class AnalyzerComponent(
    inputProvider: InputProvider,
    outputProvider: OutputProvider,
    libName: String?,
    logger: Logger,
) {
    private val apkSizeCalculator: ApkSizeCalculator = ApkSizeCalculator.getDefault()

    private val teamMapping: TeamMapping? = inputProvider.provideTeamMappingFile()?.let { moduleFile ->
        TeamMapping.createWithValidation(moduleFile, inputProvider.provideLibraryOwnershipFile(), logger)
    }

    private val dataParser: DataParser = DefaultDataParser(
        apkFileParser = DefaultApkFileParser(
            dexFileParser = DefaultDexFileParser(logger),
            apkSizeCalculator = apkSizeCalculator,
        ),
        aarFileParser = DefaultAarFileParser(DefaultJarStreamParser()),
        jarFileParser = DefaultJarFileParser(),
        inputProvider = inputProvider,
        proguardParser = DefaultProguardFileParser(logger),
    )

    private val apkComponentProcessor: ApkComponentProcessor = DefaultApkComponentProcessor(
        mapOf(
            ResourceComponentMapper::class.java to ResourceComponentMapper(),
            NativeLibComponentMapper::class.java to NativeLibComponentMapper(),
            AssetComponentMapper::class.java to AssetComponentMapper(),
            ClassComponentMapper::class.java to ClassComponentMapper(),
            OtherComponentMapper::class.java to OtherComponentMapper(),
        )
    )

    private val analyzers: Map<AnalyticsOption, Analyzer> = mapOf(
        AnalyticsOption.APK to ApkAnalyzer(
            apkComponentProcessor = apkComponentProcessor,
            dataParser = dataParser,
        ),
        AnalyticsOption.BASIC to BasicApkAnalyzer(
            dataParser = dataParser,
        ),
        AnalyticsOption.MODULES to ModuleAnalyzer(
            apkComponentProcessor = apkComponentProcessor,
            dataParser = dataParser,
            teamMapping = teamMapping,
        ),
        AnalyticsOption.TEAMS to TeamAnalyzer(
            dataParser = dataParser,
            apkComponentProcessor = apkComponentProcessor,
            teamMapping = teamMapping,
        ),
        AnalyticsOption.LIBRARIES to LibrariesAnalyzer(
            apkComponentProcessor = apkComponentProcessor,
            dataParser = dataParser,
            teamMapping = teamMapping,
        ),
        AnalyticsOption.LIB_CONTENT to LibContentAnalyzer(
            apkComponentProcessor = apkComponentProcessor,
            dataParser = dataParser,
            libName = libName,
        ),
        AnalyticsOption.LARGE_FILE to LargeFileAnalyzer(
            apkComponentProcessor = apkComponentProcessor,
            dataParser = dataParser,
            teamMapping = teamMapping,
            largeFileThreshold = inputProvider.provideLargeFileThreshold(),
        ),
    )

    private val writers: Set<ReportWriter> = setOf(
        MarkdownReportWriter(
            outputDirectory = outputProvider.provideOutPutDirectory(),
            projectInfo = outputProvider.provideProjectInfo(),
        ),
        JsonReportWriter(
            outputDirectory = outputProvider.provideOutPutDirectory(),
            projectInfo = outputProvider.provideProjectInfo(),
            customProperties = outputProvider.provideCustomProperties(),
        ),
        DatabaseReportWriter(
            reportDaoSet = lazy { DbReportDaoFactory(outputProvider, logger).create() },
            projectInfo = outputProvider.provideProjectInfo(),
            customProperties = outputProvider.provideCustomProperties(),
        ),
    )

    fun analyzerMap(): Map<AnalyticsOption, Analyzer> = analyzers

    fun reportWriters(): Set<ReportWriter> = writers
}
