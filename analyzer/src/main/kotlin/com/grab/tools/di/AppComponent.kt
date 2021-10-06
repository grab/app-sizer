package com.grab.tools.di

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.google.gson.Gson
import com.grab.tools.AnalyticsOption
import com.grab.tools.Analyzer
import com.grab.tools.AnalyzerModule
import com.grab.tools.ApkComponentAnalytic
import com.grab.tools.aar.AarFileParser
import com.grab.tools.aar.AarFileParserImpl
import com.grab.tools.analyzer.AnalyzerClass
import com.grab.tools.analyzer.ApkComponentAnalyzer
import com.grab.tools.analyzer.ApkComponentAnalyzerModule
import com.grab.tools.apk.*
import com.grab.tools.jar.JarFileParser
import com.grab.tools.jar.JarFileParserImpl
import com.grab.tools.jar.JarStreamParser
import com.grab.tools.jar.JarStreamParserImpl
import com.grab.tools.report.ReportModule
import com.grab.tools.utils.FileProviderModule
import com.grab.tools.utils.FileQuery
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import javax.inject.Named
import javax.inject.Scope

const val NAMED_DEVICE_NAME = "device_name"
const val NAMED_EXTRA_TAG = "tag"

@Scope
@Retention
annotation class AppScope

@Component(
    modules = [
        AppModule::class,
        ApkComponentAnalyzerModule::class,
        ReportModule::class,
        FileProviderModule::class,
        AnalyzerModule::class
    ]
)
@AppScope
interface AppComponent {
    fun analyzerMap(): Map<AnalyticsOption, @JvmSuppressWildcards Analyzer>

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_LIB_DIRECTORY) libsDir: File?,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_ROOT_PROJECT) rootProjectDir: File?,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE) output: File,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_FEATURE_MAPPING_FILE) featureMappingFile: File?,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE) proguardMappingFile: File?,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_APK_DIRECTORY) apkDirectory: File,
            @BindsInstance @Named(NAMED_DEVICE_NAME) deviceName: String?,
            @BindsInstance @Named(NAMED_EXTRA_TAG) extraTag: String?,
            @BindsInstance analyticsOption: AnalyticsOption
        ): AppComponent
    }
}

@Module
object AppModule {

    @Provides
    @AppScope
    fun provideDexFileParser(): DexFileParser = DexFileParserImpl()

    @Provides
    @AppScope
    fun provideApkSizeCalculator(): ApkSizeCalculator = ApkSizeCalculator.getDefault()

    @Provides
    @AppScope
    fun provideXmlPullParserFactory(): XmlPullParserFactory = XmlPullParserFactory.newInstance()

    @Provides
    @AppScope
    fun provideManifestFileParser(xmlPullParserFactory: XmlPullParserFactory): ManifestFileParser =
        ManifestFileParserImpl(xmlPullParserFactory)

    @Provides
    @AppScope
    fun provideApkParser(
        fileQuery: FileQuery,
        dexFileParser: DexFileParser,
        apkSizeCalculator: ApkSizeCalculator,
        manifestFileParser: ManifestFileParser
    ): ApkFileParser =
        ApkFileParserImpl(fileQuery, dexFileParser, apkSizeCalculator, manifestFileParser)

    @Provides
    @AppScope
    fun provideJarStreamParser(): JarStreamParser = JarStreamParserImpl()

    @Provides
    @AppScope
    fun provideJarFileParser(): JarFileParser = JarFileParserImpl()

    @Provides
    @AppScope
    fun provideAarFileParser(
        jarFileParser: JarStreamParser
    ): AarFileParser =
        AarFileParserImpl(jarFileParser)

    @Provides
    @AppScope
    fun provideApkComponentAnalytic(analytics: Map<AnalyzerClass, @JvmSuppressWildcards ApkComponentAnalyzer>): ApkComponentAnalytic =
        ApkComponentAnalytic(analytics)

    @Provides
    @AppScope
    fun provideGson() = Gson()
}