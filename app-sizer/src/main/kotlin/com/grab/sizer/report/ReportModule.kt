/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

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

