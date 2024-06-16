package com.grab.sizer.report

import com.grab.sizer.di.AppScope
import com.grab.sizer.report.db.DbReportDaoFactory
import com.grab.sizer.report.db.ReportDao
import com.grab.sizer.report.json.JsonReportWriter
import com.grab.sizer.utils.OutputProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.io.File
import javax.inject.Named

@Module
object ReportModule {
    @Provides
    fun provideCustomProperties(outputProvider: OutputProvider): CustomProperties =
        outputProvider.provideCustomProperties()

    @Provides
    @Named(NAMED_OUTPUT_DIR)
    fun provideOutputDirectory(outputProvider: OutputProvider): File = outputProvider.provideOutPutDirectory()

    @Provides
    fun provideProjectInfo(outputProvider: OutputProvider): ProjectInfo = outputProvider.provideProjectInfo()

    @Provides
    @AppScope
    fun provideReportDaoSet(reportDaoFactory: DbReportDaoFactory): Set<ReportDao> = reportDaoFactory.create()
}

@Module
interface ReportModuleBinder {
    @IntoSet
    @Binds
    fun bindMarkdownReportWriter(writer: MarkdownReportWriter): ReportWriter

    @Binds
    @IntoSet
    fun bindJsonReportWriter(writer: JsonReportWriter): ReportWriter

    @Binds
    @IntoSet
    fun bindDatabaseReportWriter(writer: DatabaseReportWriter): ReportWriter
}

