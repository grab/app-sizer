package com.grab.tools.di

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.google.gson.Gson
import com.grab.tools.AnalyticsOption
import com.grab.tools.AnalyzerModule
import com.grab.tools.aar.AarFileParser
import com.grab.tools.aar.DefaultAarFileParser
import com.grab.tools.analyzer.Analyzer
import com.grab.tools.apk.*
import com.grab.tools.jar.DefaultJarFileParser
import com.grab.tools.jar.DefaultJarStreamParser
import com.grab.tools.jar.JarFileParser
import com.grab.tools.jar.JarStreamParser
import com.grab.tools.report.ReportModule
import com.grab.tools.utils.FileProviderModule
import dagger.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import javax.inject.Named
import javax.inject.Scope

const val NAMED_DEVICE_NAME = "device_name"
const val NAMED_PROJECT_NAME = "project_name"
const val NAMED_EXTRA_TAG = "tag"
const val NAMED_LIB_NAME = "lib_name"

@Scope
@Retention
annotation class AppScope

@Component(
    modules = [
        AppModule::class,
        ApkComponentAnalyzerModule::class,
        ReportModule::class,
        FileProviderModule::class,
        AnalyzerModule::class,
        AppModuleBinder::class
    ]
)
@AppScope
interface AppComponent {
    fun analyzerMap(): Map<AnalyticsOption, @JvmSuppressWildcards Analyzer>

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_LIB_DIRECTORY) libsDir: File,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_ROOT_PROJECT) rootProjectDir: File,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE) output: File,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE) proguardMappingFile: File,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_APK_DIRECTORY) apkDirectory: File,
            @BindsInstance @Named(NAMED_DEVICE_NAME) deviceName: String,
            @BindsInstance @Named(NAMED_PROJECT_NAME) projectName: String,
            @BindsInstance @Named(NAMED_EXTRA_TAG) extraTag: String,
            @BindsInstance @Named(NAMED_LIB_NAME) libName: String?,
            @BindsInstance @AnalyzerInputFile(INPUT_FILE_FEATURE_MAPPING_FILE) featureMappingFile: File?,
            @BindsInstance analyticsOption: AnalyticsOption
        ): AppComponent
    }
}


@Module
object AppModule {
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
    fun provideGson() = Gson()
}

@Module
interface AppModuleBinder {
    @Binds
    fun DefaultDexFileParser.bindDexFileParser(): DexFileParser

    @Binds
    fun DefaultApkFileParser.bindApkParser(): ApkFileParser

    @Binds
    fun DefaultJarStreamParser.bindJarStreamParser(): JarStreamParser

    @Binds
    fun DefaultJarFileParser.bindJarFileParser(): JarFileParser

    @Binds
    fun DefaultAarFileParser.bindAarFileParser(): AarFileParser
}