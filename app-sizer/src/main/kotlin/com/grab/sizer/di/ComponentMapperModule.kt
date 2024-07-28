package com.grab.sizer.di

import com.grab.sizer.analyzer.mapper.*
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
    fun bindResourceAnalyzer(mapper: ResourceComponentMapper): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(NativeLibComponentMapper::class)
    fun bindNativeLibAnalyzer(mapper: NativeLibComponentMapper): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(AssetComponentMapper::class)
    fun bindAssetsAnalyzer(mapper: AssetComponentMapper): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(ClassComponentMapper::class)
    fun bindClassesAnalyzer(mapper: ClassComponentMapper): ComponentMapper

    @Binds
    @IntoMap
    @ClassKey(OtherComponentMapper::class)
    fun bindOtherAnalyzer(other: OtherComponentMapper): ComponentMapper

    @Binds
    fun bindApkComponentProcessor(processor: DefaultApkComponentProcessor): ApkComponentProcessor
}