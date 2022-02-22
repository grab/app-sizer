package com.grab.tools

import com.grab.tools.analyzer.*
import com.grab.tools.di.AnalyticsOptionKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
interface AnalyzerModule {
    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.GENERAL)
    fun GeneralAnalyzer.bindGeneralAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES_ANALYTICS)
    fun LibrariesAnalyzer.bindLibrariesAnalyzer(): Analyzer


    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.BASIC_APK_ANALYTICS)
    fun BasicApkAnalyzer.bindBasicApkAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun ModuleAnalyzer.bindModuleAnalyzer(): Analyzer

    @Binds
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.APK_ANALYTICS)
    fun ApkAnalyzer.bindApkAnalyzer(): Analyzer
}