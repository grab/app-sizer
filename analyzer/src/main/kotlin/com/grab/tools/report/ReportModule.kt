package com.grab.tools.report

import com.google.gson.Gson
import com.grab.pax.plugins.report.JsonFilePublisher
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.report.ExcelReportWriter
import com.grab.tools.analyzer.report.MetricsReportWriter
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.di.AnalyticsOptionKey
import com.grab.tools.di.NAMED_DEVICE_NAME
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
    fun provideApkAnalyticReport(
        reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
        @Named(NAMED_DEVICE_NAME) deviceName: String?
    ): AnalyticReport = ApkAnalyticReport(reportWriters, deviceName)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES_ANALYTICS)
    fun provideLibrariesAnalyticReport(
        reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
        @Named(NAMED_DEVICE_NAME) deviceName: String?
    ): AnalyticReport = LibrariesAnalyticReport(reportWriters, deviceName)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.FEATURES_ANALYTICS)
    fun provideFeatureAnalyticReport(
        featureMapping: FeatureMapping,
        reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
        @Named(NAMED_DEVICE_NAME) deviceName: String?
    ): AnalyticReport = FeatureAnalyticReport(featureMapping, reportWriters, deviceName)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.GENERAL)
    fun provideGeneralAnalyticReport(
        featureMapping: FeatureMapping,
        reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
        @Named(NAMED_DEVICE_NAME) deviceName: String?
    ): AnalyticReport = GeneralAnalyticReport(featureMapping, reportWriters, deviceName)


    @Provides
    @IntoSet
    fun provideExcelFeatureReportWriter(@Named(NAMED_OUTPUT_FILE) file: File): ReportWriter =
        ExcelReportWriter(file.toExcelFile())

    @Provides
    @IntoSet
    fun provideJsonFeatureReportWriter(metricsPublisher: MetricsPublisher): ReportWriter =
        MetricsReportWriter(metricsPublisher)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun provideModuleAnalyticReport(
        @Named(NAMED_OUTPUT_FILE) outPutFile: File,
    ): AnalyticReport = ModuleAnalyticReport(outPutFile)

    @Provides
    fun provideJsonFilePublisher(@Named(NAMED_OUTPUT_FILE) file: File, gson : Gson): MetricsPublisher =
        JsonFilePublisher(file.toJsonFile(), gson)
}

private fun File.toExcelFile(): File = File(parentFile, "$nameWithoutExtension.xls")
private fun File.toJsonFile(): File = File(parentFile, "$nameWithoutExtension.json")