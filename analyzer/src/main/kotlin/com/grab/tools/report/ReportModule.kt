package com.grab.tools.report

import com.google.gson.Gson
import com.grab.pax.plugins.report.JsonFilePublisher
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.tools.analyzer.report.AgentReportWriter
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.analyzer.report.XlsReportWriter
import com.grab.tools.di.*
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
    fun provideXlsReportWriter(@AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE) file: File): ReportWriter =
        XlsReportWriter(file.toExcelFile())

    @Provides
    @IntoSet
    fun provideAgentReportWriter(metricsPublisher: MetricsPublisher): ReportWriter = AgentReportWriter(metricsPublisher)

    @Provides
    fun provideJsonFilePublisher(@AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE) file: File, gson: Gson): MetricsPublisher =
        JsonFilePublisher(file.toJsonFile(), gson)

    @Provides
    fun provideProjectInfoFactory(
        @Named(NAMED_DEVICE_NAME) deviceName: String,
        @Named(NAMED_PROJECT_NAME) projectName: String,
        @Named(NAMED_EXTRA_TAG) pipelineId: String,
    ) = ProjectInfoFactory(deviceName = deviceName, projectName = projectName, pipelineId = pipelineId)
}

private fun File.toExcelFile(): File = File(parentFile, "$nameWithoutExtension.xls")
private fun File.toJsonFile(): File = File(parentFile, "$nameWithoutExtension.json")