package com.grab.sizer.report

import com.google.gson.Gson
import com.grab.pax.plugins.JsonFilePublisher
import com.grab.pax.plugins.MetricsPublisher
import com.grab.sizer.di.AppScope
import com.grab.sizer.report.db.DbReportDaoFactory
import com.grab.sizer.report.db.ReportDao
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.OutputProvider
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object ReportModule {

    @Provides
    fun provideFeatureMapping(
        inputProvider: InputProvider
    ): FeatureMapping {
        val ownerMapping = inputProvider.provideFeatureMappingFile()
        return if (ownerMapping == null) DummyFeatureMapping()
        else YmlFeatureMapping(ownerMapping)
    }

    @Provides
    @IntoSet
    fun provideXlsReportWriter(inputProvider: OutputProvider): ReportWriter =
        XlsReportWriter(inputProvider.provideOutPutDirectory())

    @Provides
    @IntoSet
    fun provideAgentReportWriter(metricsPublisher: MetricsPublisher): ReportWriter = AgentReportWriter(metricsPublisher)

    @Provides
    @IntoSet
    fun provideDatabaseWriter(reportDaoSet: Lazy<Set<ReportDao>>): ReportWriter = DatabaseReportWriter(reportDaoSet)

    @Provides
    @AppScope
    fun provideReportDaoSet(reportDaoFactory: DbReportDaoFactory): Set<ReportDao> = reportDaoFactory.create()

    @Provides
    fun provideJsonFilePublisher(inputProvider: OutputProvider, gson: Gson): MetricsPublisher =
        JsonFilePublisher(inputProvider.provideOutPutDirectory(), gson)
}