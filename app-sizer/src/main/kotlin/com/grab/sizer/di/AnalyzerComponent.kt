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

package com.grab.sizer.di

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.Analyzer
import com.grab.sizer.report.ReportModule
import com.grab.sizer.report.ReportModuleBinder
import com.grab.sizer.report.ReportWriter
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider
import com.grab.sizer.SizeCalculationMode
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named

internal const val NAMED_LIB_NAME = "lib_name"

@Component(
    modules = [
        AnalyzerModule::class,
        ComponentMapperModule::class,
        AnalyzerBinder::class,
        ParserBinder::class,
        ReportModule::class,
        ReportModuleBinder::class
    ]
)
@AppScope
interface AnalyzerComponent {
    fun analyzerMap(): Map<AnalyticsOption, @JvmSuppressWildcards Analyzer>
    fun reportWriters(): Set<@JvmSuppressWildcards ReportWriter>

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance inputProvider: InputProvider,
            @BindsInstance outputProvider: OutputProvider,
            @BindsInstance sizeCalculationMode: SizeCalculationMode,
            @BindsInstance @Named(NAMED_LIB_NAME) libName: String?,
            @BindsInstance logger: Logger,
        ): AnalyzerComponent
    }
}

