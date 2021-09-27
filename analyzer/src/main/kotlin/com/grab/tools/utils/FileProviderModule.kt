package com.grab.tools.utils

import com.grab.tools.AnalyticsOption
import com.grab.tools.di.AnalyticsOptionKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
object FileProviderModule {
    @Provides
    fun provideFileQuery(): FileQuery = DefaultFileQuery()

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES_ANALYTICS)
    fun provideDefaultAarFileProvider(fileQuery: FileQuery): AarFileProvider = DefaultAarFileProvider(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun provideModuleAnalyticAarFileProvider(fileQuery: FileQuery): AarFileProvider = ModuleAarFileProvider(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.FEATURES_ANALYTICS)
    fun provideFeatureAnalyticAarFileProvider(fileQuery: FileQuery): AarFileProvider = ModuleAarFileProvider(fileQuery)

    @Provides
    fun provideAarFileProvider(
        analyticsOption: AnalyticsOption,
        map: Map<AnalyticsOption, @JvmSuppressWildcards AarFileProvider>
    ): AarFileProvider = map[analyticsOption]!!

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES_ANALYTICS)
    fun provideDefaultJarFileProvider(fileQuery: FileQuery): JarFileProvider = DefaultJarFileProvider(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun provideModuleModuleJarFileProvider(fileQuery: FileQuery): JarFileProvider = ModuleJarFileProvider(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.FEATURES_ANALYTICS)
    fun provideFeatureModuleJarFileProvider(fileQuery: FileQuery): JarFileProvider = ModuleJarFileProvider(fileQuery)

    @Provides
    fun provideJarFileProvider(
        analyticsOption: AnalyticsOption,
        map: Map<AnalyticsOption, @JvmSuppressWildcards JarFileProvider>
    ): JarFileProvider = map[analyticsOption]!!
}