package com.grab.tools.analyzer

import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap


typealias AnalyzerClass = Class<*>

@Module
object ApkComponentAnalyzerModule {
    @Provides
    @IntoMap
    @ClassKey(ResourceApkComponentAnalyzer::class)
    fun provideResourceAnalyzer(): ApkComponentAnalyzer = ResourceApkComponentAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(NativeLibApkComponentAnalyzer::class)
    fun provideNativeLibAnalyzer(): ApkComponentAnalyzer = NativeLibApkComponentAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(AssetsApkComponentAnalyzer::class)
    fun provideAssetsAnalyzer(): ApkComponentAnalyzer = AssetsApkComponentAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(ClassesApkComponentAnalyzer::class)
    fun provideClassesAnalyzer(): ApkComponentAnalyzer = ClassesApkComponentAnalyzer()

    @Provides
    @IntoMap
    @ClassKey(OtherApkComponentAnalyzer::class)
    fun provideOtherAnalyzer(): ApkComponentAnalyzer = OtherApkComponentAnalyzer()
}