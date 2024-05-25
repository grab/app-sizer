package com.grab.sizer.report

import com.grab.sizer.di.AppScope
import com.grab.sizer.report.db.DbReportDaoFactory
import com.grab.sizer.report.db.ReportDao
import com.grab.sizer.report.json.JsonReportWriter
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.OutputProvider
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object ReportModule {

    @Provides
    fun provideTeamMapping(
        inputProvider: InputProvider
    ): TeamMapping {
        val ownerMapping = inputProvider.provideTeamMappingFile()
        return if (ownerMapping == null) DummyTeamMapping()
        else YmlTeamMapping(ownerMapping)
    }

    @Provides
    @IntoSet
    fun provideXlsReportWriter(inputProvider: OutputProvider): ReportWriter =
        XlsReportWriter(inputProvider.provideOutPutDirectory())

    @Provides
    @IntoSet
    fun provideAgentReportWriter(inputProvider: OutputProvider): ReportWriter = JsonReportWriter(
        inputProvider.provideOutPutDirectory()
    )

    @Provides
    @IntoSet
    fun provideDatabaseWriter(reportDaoSet: Lazy<Set<ReportDao>>): ReportWriter = DatabaseReportWriter(reportDaoSet)

    @Provides
    @AppScope
    fun provideReportDaoSet(reportDaoFactory: DbReportDaoFactory): Set<ReportDao> = reportDaoFactory.create()
}