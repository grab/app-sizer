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
import com.google.gson.Gson
import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.*
import com.grab.sizer.parser.DataParser
import com.grab.sizer.parser.DefaultDataParser
import com.grab.sizer.utils.InputProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention
annotation class AppScope

@Module
object AnalyzerModule {
    @Provides
    @AppScope
    fun provideApkSizeCalculator(): ApkSizeCalculator = ApkSizeCalculator.getDefault()

    @Provides
    @AppScope
    fun provideGson() = Gson()

    @Provides
    fun provideTeamMapping(
        inputProvider: InputProvider
    ): TeamMapping {
        // Todo : Remove this logic from dagger module, possible remove DummyTeamMapping
        val ownerMapping = inputProvider.provideTeamMappingFile()
        return if (ownerMapping == null) DummyTeamMapping()
        else YmlTeamMapping(ownerMapping)
    }

    @Provides
    @Named("largeFileThreshold")
    fun provideLargeFileThreshold(inputProvider: InputProvider): Long = inputProvider.provideLargeFileThreshold()
}


@Module
internal interface AnalyzerBinder {
    @Binds
    fun bindDataParser(parser: DefaultDataParser): DataParser

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.CODEBASE)
    fun bindGeneralAnalyzer(analyzer: CodebaseAnalyzer): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES)
    fun bindLibrariesAnalyzer(analyzer: LibrariesAnalyzer): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIB_CONTENT)
    fun bindAnalyzer(analyzer: LibContentAnalyzer): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.BASIC)
    fun bindBasicApkAnalyzer(analyzer: BasicApkAnalyzer): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULES)
    fun bindModuleAnalyzer(analyzer: ModuleAnalyzer): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.APK)
    fun bindApkAnalyzer(analyzer: ApkAnalyzer): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LARGE_FILE)
    fun bindLargeFileAnalyzer(analyser: LargeFileAnalyzer): Analyzer
}