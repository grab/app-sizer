package com.grab.tools.di

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.Analyzer
import com.grab.tools.apk.*
import com.grab.tools.report.ProjectInfoProvider
import com.grab.tools.report.ReportModule
import com.grab.tools.utils.InputFileProvider
import dagger.*
import javax.inject.Named

internal const val NAMED_DEVICE_NAME = "device_name"
internal const val NAMED_PROJECT_NAME = "project_name"
internal const val NAMED_EXTRA_TAG = "tag"
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
//            @BindsInstance analyticsOption: AnalyticsOption,
            @BindsInstance projectInfoProvider: ProjectInfoProvider,
            @BindsInstance @Named(NAMED_LIB_NAME) libName: String?,
        ): AnalyzerComponent
    }
}

