package com.grab.tools.report

import com.grab.tools.AnalyticsOption
import com.grab.tools.di.AnalyticsOptionKey
import com.grab.tools.di.NAMED_FEATURE_MAPPING_FILE
import com.grab.tools.di.NAMED_OUTPUT_FILE
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import java.io.File
import javax.inject.Named

@Module
object ReportModule {

    @Provides
    fun provideFeatureMapping(
        @Named(NAMED_FEATURE_MAPPING_FILE) ymlFile: File?
    ): FeatureMapping {
        return if (ymlFile == null) DummyFeatureMapping()
        else DefaultFeatureMapping(ymlFile)
    }

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.APK_ANALYTICS)
    fun provideApkAnalyticReport(): AnalyticReport = ApkAnalyticReport()

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES_ANALYTICS)
    fun provideLibrariesAnalyticReport(
        @Named(NAMED_OUTPUT_FILE) outPutFile: File
    ): AnalyticReport = LibrariesAnalyticReport(outPutFile)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.FEATURES_ANALYTICS)
    fun provideFeatureAnalyticReport(
        featureMapping: FeatureMapping,
        featureReportWriter: Set<@JvmSuppressWildcards FeatureReportWriter>
    ): AnalyticReport = FeatureAnalyticReport(featureMapping, featureReportWriter)

    @Provides
    @IntoSet
    fun provideExcelFeatureReportWriter(@Named(NAMED_OUTPUT_FILE) rootProject: File): FeatureReportWriter =
        ExcelFeatureReportWriter(rootProject)

    @Provides
    @IntoSet
    fun provideJsonFeatureReportWriter(@Named(NAMED_OUTPUT_FILE) rootProject: File): FeatureReportWriter =
        JsonFeatureReportWriter(rootProject)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun provideModuleAnalyticReport(
        @Named(NAMED_OUTPUT_FILE) outPutFile: File,
    ): AnalyticReport = ModuleAnalyticReport(outPutFile)
}