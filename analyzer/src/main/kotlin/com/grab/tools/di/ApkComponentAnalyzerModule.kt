package com.grab.tools.di

import com.grab.tools.analyzer.apk.*
import com.grab.tools.analyzer.apk.ApkComponentProcessor
import com.grab.tools.analyzer.apk.DefaultApkComponentProcessor
import dagger.Binds
import dagger.Module
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