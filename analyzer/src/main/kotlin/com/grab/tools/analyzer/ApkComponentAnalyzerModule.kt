package com.grab.tools.analyzer

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap


typealias AnalyzerClass = Class<*>

@Module
interface ApkComponentAnalyzerModule {
    @Binds
    @IntoMap
    @ClassKey(ResourceApkComponentAnalyzer::class)
    fun ResourceApkComponentAnalyzer.bindResourceAnalyzer(): ApkComponentAnalyzer

    @Binds
    @IntoMap
    @ClassKey(NativeLibApkComponentAnalyzer::class)
    fun NativeLibApkComponentAnalyzer.bindNativeLibAnalyzer(): ApkComponentAnalyzer

    @Binds
    @IntoMap
    @ClassKey(AssetsApkComponentAnalyzer::class)
    fun AssetsApkComponentAnalyzer.bindAssetsAnalyzer(): ApkComponentAnalyzer

    @Binds
    @IntoMap
    @ClassKey(ClassesApkComponentAnalyzer::class)
    fun ClassesApkComponentAnalyzer.bindClassesAnalyzer(): ApkComponentAnalyzer

    @Binds
    @IntoMap
    @ClassKey(OtherApkComponentAnalyzer::class)
    fun OtherApkComponentAnalyzer.bindOtherAnalyzer(): ApkComponentAnalyzer


    @Binds
    fun DefaultApkComponentProcessor.bindApkComponentProcessor(): ApkComponentProcessor
}

//@Module
//object ApkComponentAnalyzerModule {
//    @Provides
//    @IntoMap
//    @ClassKey(ResourceApkComponentAnalyzer::class)
//    fun ResourceApkComponentAnalyzer.bindResourceAnalyzer(): ApkComponentAnalyzer = this
//
//    @Provides
//    @IntoMap
//    @ClassKey(NativeLibApkComponentAnalyzer::class)
//    fun NativeLibApkComponentAnalyzer.bindNativeLibAnalyzer(): ApkComponentAnalyzer = this
//
//    @Provides
//    @IntoMap
//    @ClassKey(AssetsApkComponentAnalyzer::class)
//    fun AssetsApkComponentAnalyzer.bindAssetsAnalyzer(): ApkComponentAnalyzer = this
//
//    @Provides
//    @IntoMap
//    @ClassKey(ClassesApkComponentAnalyzer::class)
//    fun ClassesApkComponentAnalyzer.bindClassesAnalyzer(): ApkComponentAnalyzer = this
//
//    @Provides
//    @IntoMap
//    @ClassKey(OtherApkComponentAnalyzer::class)
//    fun OtherApkComponentAnalyzer.bindOtherAnalyzer(): ApkComponentAnalyzer = this
//
//
//    @Binds
//    fun DefaultApkComponentProcessor.bindApkComponentProcessor(): ApkComponentProcessor
//}