package com.grab.bundle

import com.grab.bundle.aar.AarFileParser
import com.grab.bundle.aar.AarFileParserImpl
import com.grab.bundle.analyzer.*
import com.grab.bundle.apk.ApkFileParser
import com.grab.bundle.apk.ApkFileParserImpl
import com.grab.bundle.apk.DexFileParser
import com.grab.bundle.apk.DexFileParserImpl
import com.grab.bundle.jar.JarFileParser
import com.grab.bundle.jar.JarFileParserImpl
import com.grab.bundle.jar.JarStreamParser
import com.grab.bundle.jar.JarStreamParserImpl
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
    fun aarFileParser() : AarFileParser
    fun jarFileParser() : JarFileParser
    fun analytics(): Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Analyzer>
}

@Module
object ApkProfileModule {
    @Provides
    @Singleton
    fun provideFileQuery(): FileQuery = FileQuery()

    @Provides
    @Singleton
    fun provideDexFileParser(): DexFileParser = DexFileParserImpl()

    @Provides
    @Singleton
    fun provideApkParser(fileQuery: FileQuery, dexFileParser: DexFileParser): ApkFileParser =
        ApkFileParserImpl(fileQuery, dexFileParser)

    @Provides
    @Singleton
    fun provideJarStreamParser(): JarStreamParser = JarStreamParserImpl()

    @Provides
    @Singleton
    fun provideJarFileParser(fileQuery: FileQuery): JarFileParser = JarFileParserImpl(fileQuery)

    @Provides
    @Singleton
    fun provideAarFileParser(fileQuery: FileQuery, jarFileParser: JarStreamParser): AarFileParser =
        AarFileParserImpl(fileQuery, jarFileParser)

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
    fun provideApkComponentAnalytic(
        apkFileParser: ApkFileParser,
        aarFileParser: AarFileParser,
        jarFileParser: JarFileParser,
        analytics: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Analyzer>
    ): ApkComponentAnalytic {
        return ApkComponentAnalytic(apkFileParser, aarFileParser, jarFileParser, analytics)
    }
}