package com.grab.tools.di

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.Analyzer
import com.grab.tools.analyzer.ProjectInfoProvider
import com.grab.tools.analyzer.report.ReportModule
import com.grab.tools.utils.InputFileProvider
import com.grab.tools.utils.Logger
import dagger.*
import javax.inject.Named

internal const val NAMED_LIB_NAME = "lib_name"

@Component(
    modules = [
        AnalyzerModule::class,
        ApkComponentAnalyzerModule::class,
        ReportModule::class,
        AnalyzerBinder::class,
        ParserBinder::class
    ]
)
@AppScope
interface AnalyzerComponent {
    fun analyzerMap(): Map<AnalyticsOption, @JvmSuppressWildcards Analyzer>

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance inputFileProvider: InputFileProvider,
            @BindsInstance projectInfoProvider: ProjectInfoProvider,
            @BindsInstance @Named(NAMED_LIB_NAME) libName: String?,
            @BindsInstance logger: Logger,
        ): AnalyzerComponent
    }
}

