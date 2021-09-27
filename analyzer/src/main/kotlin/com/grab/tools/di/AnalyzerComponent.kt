package com.grab.tools.di

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.tools.AnalyticsOption
import com.grab.tools.ApkComponentAnalytic
import com.grab.tools.aar.AarFileParser
import com.grab.tools.aar.AarFileParserImpl
import com.grab.tools.analyzer.Analyzer
import com.grab.tools.analyzer.AnalyzerClass
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ApkFileParserImpl
import com.grab.tools.apk.DexFileParser
import com.grab.tools.apk.DexFileParserImpl
import com.grab.tools.jar.JarFileParser
import com.grab.tools.jar.JarFileParserImpl
import com.grab.tools.jar.JarStreamParser
import com.grab.tools.jar.JarStreamParserImpl
import com.grab.tools.report.AnalyticReport
import com.grab.tools.report.ReportModule
import com.grab.tools.utils.AarFileProvider
import com.grab.tools.utils.FileProviderModule
import com.grab.tools.utils.FileQuery
import com.grab.tools.utils.JarFileProvider
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named
import javax.inject.Scope

const val NAMED_LIB_DIRECTORY = "lib"
const val NAMED_OUTPUT_FILE = "out"
const val NAMED_FEATURE_MAPPING_FILE = "mapping_file"

@Scope
@Retention
annotation class AppScope

@Component(
    modules = [
        AnalyzerModule::class,
        com.grab.tools.analyzer.AnalyzerModule::class,
        ReportModule::class,
        FileProviderModule::class
    ]
)
@AppScope
interface AnalyzerComponent {
    fun apkComponentAnalytic(): ApkComponentAnalytic
    fun apkParser(): ApkFileParser
    fun aarFileParser(): AarFileParser
    fun jarFileParser(): JarFileParser
    fun analyticReportMap(): Map<AnalyticsOption, @JvmSuppressWildcards AnalyticReport>

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @Named(NAMED_LIB_DIRECTORY) libsDir: File,
            @BindsInstance @Named(NAMED_OUTPUT_FILE) output: File,
            @BindsInstance @Named(NAMED_FEATURE_MAPPING_FILE) featureMappingFile: File?,
            @BindsInstance analyticsOption: AnalyticsOption
        ): AnalyzerComponent
    }
}

@Module
object AnalyzerModule {

    @Provides
    @AppScope
    fun provideDexFileParser(): DexFileParser = DexFileParserImpl()

    @Provides
    @AppScope
    fun provideApkSizeCalculator(): ApkSizeCalculator = ApkSizeCalculator.getDefault()

    @Provides
    @AppScope
    fun provideApkParser(
        fileQuery: FileQuery,
        dexFileParser: DexFileParser,
        apkSizeCalculator: ApkSizeCalculator
    ): ApkFileParser =
        ApkFileParserImpl(fileQuery, dexFileParser, apkSizeCalculator)

    @Provides
    @AppScope
    fun provideJarStreamParser(): JarStreamParser = JarStreamParserImpl()

    @Provides
    @AppScope
    fun provideJarFileParser(jarFileProvider: JarFileProvider): JarFileParser = JarFileParserImpl(jarFileProvider)

    @Provides
    @AppScope
    fun provideAarFileParser(
        jarFileParser: JarStreamParser,
        aarFileProvider: AarFileProvider
    ): AarFileParser =
        AarFileParserImpl(aarFileProvider, jarFileParser)

    @Provides
    @AppScope
    fun provideApkComponentAnalytic(analytics: Map<AnalyzerClass, @JvmSuppressWildcards Analyzer>): ApkComponentAnalytic =
        ApkComponentAnalytic(analytics)
}