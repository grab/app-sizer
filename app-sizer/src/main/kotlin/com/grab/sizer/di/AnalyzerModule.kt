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