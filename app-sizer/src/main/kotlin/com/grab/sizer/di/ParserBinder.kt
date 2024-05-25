package com.grab.sizer.di

import com.grab.sizer.parser.*
import dagger.Binds
import dagger.Module

@Module
internal interface ParserBinder {
    @Binds
    fun DefaultDexFileParser.bindDexFileParser(): DexFileParser

    @Binds
    fun DefaultApkFileParser.bindApkParser(): ApkFileParser

    @Binds
    fun DefaultJarStreamParser.bindJarStreamParser(): JarStreamParser

    @Binds
    fun DefaultJarFileParser.bindJarFileParser(): JarFileParser

    @Binds
    fun DefaultAarFileParser.bindAarFileParser(): AarFileParser

    @Binds
    fun DefaultProguardFileParser.bindProguardFileParser(): ProguardFileParser
}