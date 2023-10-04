package com.grab.tools.di

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.Analyzer
import com.grab.tools.apk.*
import com.grab.tools.report.ReportModule
import dagger.*
import java.io.File
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
        ParserBinder::class,
        CltModule::class,
        CltBinder::class
    ]
)
@AppScope
interface CltComponent {
    fun analyzerMap(): Map<AnalyticsOption, @JvmSuppressWildcards Analyzer>

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @AnalyzerInputFile(INPUT_LIB_DIRECTORY) libsDir: File,
            @BindsInstance @AnalyzerInputFile(INPUT_ROOT_PROJECT) rootProjectDir: File,
            @BindsInstance @AnalyzerInputFile(INPUT_OUTPUT_FILE) output: File,
            @BindsInstance @AnalyzerInputFile(INPUT_PROGUARD_MAPPING_FILE) proguardMappingFile: File,
            @BindsInstance @AnalyzerInputFile(INPUT_APK_DIRECTORY) apkDirectory: File,
            @BindsInstance @Named(NAMED_DEVICE_NAME) deviceName: String,
            @BindsInstance @Named(NAMED_PROJECT_NAME) projectName: String,
            @BindsInstance @Named(NAMED_EXTRA_TAG) extraTag: String,
            @BindsInstance @Named(NAMED_LIB_NAME) libName: String?,
            @BindsInstance @AnalyzerInputFile(INPUT_FEATURE_MAPPING_FILE) featureMappingFile: File?,
            @BindsInstance analyticsOption: AnalyticsOption
        ): CltComponent
    }
}

