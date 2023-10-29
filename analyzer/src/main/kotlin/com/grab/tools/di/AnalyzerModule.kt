package com.grab.tools.di

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.google.gson.Gson
import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.*
import com.grab.tools.apk.ManifestFileParser
import com.grab.tools.apk.ManifestFileParserImpl
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
    fun provideManifestFileParser(xmlPullParserFactory: XmlPullParserFactory): ManifestFileParser =
        ManifestFileParserImpl(xmlPullParserFactory)

    @Provides
    @AppScope
    fun provideGson() = Gson()
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
    @AnalyticsOptionKey(AnalyticsOption.BASIC_APK)
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