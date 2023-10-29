package com.grab.tools.di

import com.grab.tools.analyzer.mapper.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap


typealias AnalyzerClass = Class<*>

@Module
internal interface ComponentMapperModule {
    @Binds
    @IntoMap
    @ClassKey(ResourceComponentMapper::class)
    fun ResourceComponentMapper.bindResourceAnalyzer(): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(NativeLibComponentMapper::class)
    fun NativeLibComponentMapper.bindNativeLibAnalyzer(): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(AssetsComponentMapper::class)
    fun AssetsComponentMapper.bindAssetsAnalyzer(): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(ClassesComponentMapper::class)
    fun ClassesComponentMapper.bindClassesAnalyzer(): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(OtherComponentMapper::class)
    fun OtherComponentMapper.bindOtherAnalyzer(): ComponentMapper


    @Binds
    fun DefaultApkComponentProcessor.bindApkComponentProcessor(): ApkComponentProcessor
}