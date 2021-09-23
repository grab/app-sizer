package com.grab.tools

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.tools.aar.AarFileParser
import com.grab.tools.aar.AarFileParserImpl
import com.grab.tools.analyzer.*
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.ApkFileParserImpl
import com.grab.tools.apk.DexFileParser
import com.grab.tools.apk.DexFileParserImpl
import com.grab.tools.jar.JarFileParser
import com.grab.tools.jar.JarFileParserImpl
import com.grab.tools.jar.JarStreamParser
import com.grab.tools.jar.JarStreamParserImpl
import com.grab.tools.utils.DefaultAarQuery
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Component(
    modules = [ApkProfileModule::class]
)
@Singleton
interface ApkProfileComponent {
    fun apkComponentAnalytic(): ApkComponentAnalytic
    fun apkParser(): ApkFileParser
    fun aarFileParser(): AarFileParser
    fun jarFileParser(): JarFileParser
    fun analytics(): Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Analyzer>
}

@Module
object ApkProfileModule {
    @Provides
    @Singleton
    fun provideFileQuery(): FileQuery = DefaultFileQuery()

    @Provides
    @Singleton
    fun provideDexFileParser(): DexFileParser = DexFileParserImpl()

    @Provides
    @Singleton
    fun provideApkSizeCalculator(): ApkSizeCalculator = ApkSizeCalculator.getDefault()

    @Provides
    @Singleton
    fun provideApkParser(
        fileQuery: FileQuery,
        dexFileParser: DexFileParser,
        apkSizeCalculator: ApkSizeCalculator
    ): ApkFileParser =
        ApkFileParserImpl(fileQuery, dexFileParser, apkSizeCalculator)

    @Provides
    @Singleton
    fun provideJarStreamParser(): JarStreamParser = JarStreamParserImpl()

    @Provides
    @Singleton
    fun provideJarFileParser(fileQuery: FileQuery): JarFileParser = JarFileParserImpl(fileQuery)

    @Provides
    @Singleton
    fun provideAarFileParser(fileQuery: FileQuery, jarFileParser: JarStreamParser): AarFileParser =
        AarFileParserImpl(DefaultAarQuery(fileQuery), jarFileParser)

    @Provides
    @Singleton
    @JvmStatic
    fun provideAnalytics(): Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Analyzer> = mapOf(
        ResourceAnalyzer.TAG to ResourceAnalyzer(),
        NativeLibAnalyzer.TAG to NativeLibAnalyzer(),
        AssetsAnalyzer.TAG to AssetsAnalyzer(),
        ClassesAnalyzer.TAG to ClassesAnalyzer(),
        OtherAnalyzer.TAG to OtherAnalyzer()
    )

    @Provides
    @Singleton
    fun provideApkComponentAnalytic(analytics: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Analyzer>): ApkComponentAnalytic {
        return ApkComponentAnalytic(analytics)
    }
}