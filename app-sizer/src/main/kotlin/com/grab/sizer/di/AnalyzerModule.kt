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
import org.xmlpull.v1.XmlPullParserFactory
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
    fun provideXmlPullParserFactory(): XmlPullParserFactory = XmlPullParserFactory.newInstance()

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
}


@Module
internal interface AnalyzerBinder {
    @Binds
    fun DefaultDataParser.bindDataParser(): DataParser

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.CODEBASE)
    fun CodebaseAnalyzer.bindGeneralAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES)
    fun LibrariesAnalyzer.bindLibrariesAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIB_CONTENT)
    fun LibContentAnalyzer.bindAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.BASIC)
    fun BasicApkAnalyzer.bindBasicApkAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULES)
    fun ModuleAnalyzer.bindModuleAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.APK)
    fun ApkAnalyzer.bindApkAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LARGE_FILE)
    fun LargeFileAnalyzer.bindLargeFileAnalyzer(): Analyzer
}