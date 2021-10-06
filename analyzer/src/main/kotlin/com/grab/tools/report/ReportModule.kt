package com.grab.tools.report

import com.google.gson.Gson
import com.grab.pax.plugins.report.JsonFilePublisher
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.tools.analyzer.report.ExcelReportWriter
import com.grab.tools.analyzer.report.MetricsReportWriter
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.di.AnalyzerInputFile
import com.grab.tools.di.INPUT_FILE_FEATURE_MAPPING_FILE
import com.grab.tools.di.INPUT_FILE_OUTPUT_FILE
import com.grab.tools.di.NAMED_EXTRA_TAG
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.io.File
import javax.inject.Named

@Module
object ReportModule {

    @Provides
    fun provideFeatureMapping(
        @AnalyzerInputFile(INPUT_FILE_FEATURE_MAPPING_FILE) ymlFile: File?
    ): FeatureMapping {
        return if (ymlFile == null) DummyFeatureMapping()
        else DefaultFeatureMapping(ymlFile)
    }

    @Provides
    @IntoSet
    fun provideExcelFeatureReportWriter(@AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE) file: File): ReportWriter =
        ExcelReportWriter(file.toExcelFile())

    @Provides
    @IntoSet
    fun provideMetricsReportWriter(
        @Named(NAMED_EXTRA_TAG) extraTag: String?,
        metricsPublisher: MetricsPublisher
    ): ReportWriter =
        MetricsReportWriter(metricsPublisher, extraTag ?: "0000000")

    @Provides
    fun provideJsonFilePublisher(@AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE) file: File, gson: Gson): MetricsPublisher =
        JsonFilePublisher(file.toJsonFile(), gson)
}

private fun File.toExcelFile(): File = File(parentFile, "$nameWithoutExtension.xls")
private fun File.toJsonFile(): File = File(parentFile, "$nameWithoutExtension.json")