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
    fun provideDefaultAarFileProvider(fileQuery: FileQuery): AarFileQuery = DefaultAarFileQuery(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun provideModuleAnalyticAarFileProvider(fileQuery: FileQuery): AarFileQuery = ModuleAarFileQuery(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.FEATURES_ANALYTICS)
    fun provideFeatureAnalyticAarFileProvider(fileQuery: FileQuery): AarFileQuery = ModuleAarFileQuery(fileQuery)

    @Provides
    fun provideAarFileProvider(
        analyticsOption: AnalyticsOption,
        map: Map<AnalyticsOption, @JvmSuppressWildcards AarFileQuery>
    ): AarFileQuery = map[analyticsOption]!!

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.LIBRARIES_ANALYTICS)
    fun provideDefaultJarFileProvider(fileQuery: FileQuery): JarFileQuery = DefaultJarFileQuery(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.MODULE_ANALYTICS)
    fun provideModuleModuleJarFileProvider(fileQuery: FileQuery): JarFileQuery = ModuleJarFileQuery(fileQuery)

    @Provides
    @IntoMap
    @AnalyticsOptionKey(AnalyticsOption.FEATURES_ANALYTICS)
    fun provideFeatureModuleJarFileProvider(fileQuery: FileQuery): JarFileQuery = ModuleJarFileQuery(fileQuery)

    @Provides
    fun provideJarFileProvider(
        analyticsOption: AnalyticsOption,
        map: Map<AnalyticsOption, @JvmSuppressWildcards JarFileQuery>
    ): JarFileQuery = map[analyticsOption]!!
}