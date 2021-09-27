package com.grab.tools.analyzer

import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap


typealias AnalyzerClass = Class<*>

@Module
object AnalyzerModule {
    @Provides
    @IntoMap
    @ClassKey(ResourceAnalyzer::class)
    fun provideResourceAnalyzer(): Analyzer = ResourceAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(NativeLibAnalyzer::class)
    fun provideNativeLibAnalyzer(): Analyzer = NativeLibAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(AssetsAnalyzer::class)
    fun provideAssetsAnalyzer(): Analyzer = AssetsAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(ClassesAnalyzer::class)
    fun provideClassesAnalyzer(): Analyzer = ClassesAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(OtherAnalyzer::class)
    fun provideOtherAnalyzer(): Analyzer = OtherAnalyzer()
}