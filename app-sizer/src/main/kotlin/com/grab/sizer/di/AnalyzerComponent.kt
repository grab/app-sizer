package com.grab.sizer.di

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.Analyzer
import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.report.ReportModule
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named

internal const val NAMED_LIB_NAME = "lib_name"

@Component(
    modules = [
        AnalyzerModule::class,
        ComponentMapperModule::class,
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
            @BindsInstance inputProvider: InputProvider,
            @BindsInstance outputProvider: OutputProvider,
            @BindsInstance projectInfoProvider: ProjectInfoProvider,
            @BindsInstance @Named(NAMED_LIB_NAME) libName: String?,
            @BindsInstance logger: Logger,
        ): AnalyzerComponent
    }
}

