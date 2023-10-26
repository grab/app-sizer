package com.grab.tools.report

import com.google.gson.Gson
import com.grab.pax.plugins.report.JsonFilePublisher
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.tools.analyzer.report.AgentReportWriter
import com.grab.tools.analyzer.report.ReportWriter
import com.grab.tools.analyzer.report.XlsReportWriter
import com.grab.tools.utils.InputFileProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

const val NAMED_DEVICE_NAME = "device_name"
const val NAMED_PROJECT_NAME = "project_name"
const val NAMED_EXTRA_TAG = "tag"
const val NAMED_LIB_NAME = "lib_name"

@Module
object ReportModule {

    @Provides
    fun provideFeatureMapping(
        inputFileProvider: InputFileProvider
    ): FeatureMapping {
        val ownerMapping = inputFileProvider.provideFeatureMappingFile()
        return if (ownerMapping == null) DummyFeatureMapping()
        else YmlFeatureMapping(ownerMapping)
    }

    @Provides
    @IntoSet
    fun provideXlsReportWriter(inputFileProvider: InputFileProvider): ReportWriter =
        XlsReportWriter(inputFileProvider.provideOutPutDirectory())

    @Provides
    @IntoSet
    fun provideAgentReportWriter(metricsPublisher: MetricsPublisher): ReportWriter = AgentReportWriter(metricsPublisher)

    @Provides
    fun provideJsonFilePublisher(inputFileProvider: InputFileProvider, gson: Gson): MetricsPublisher =
        JsonFilePublisher(inputFileProvider.provideOutPutDirectory(), gson)
}