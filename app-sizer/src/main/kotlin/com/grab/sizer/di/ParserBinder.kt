package com.grab.sizer.di

import com.grab.sizer.parser.*
import dagger.Binds
import dagger.Module

@Module
internal interface ParserBinder {
    @Binds
    fun bindDexFileParser(parser: DefaultDexFileParser): DexFileParser

    @Binds
    fun bindApkParser(parser: DefaultApkFileParser): ApkFileParser

    @Binds
    fun bindJarStreamParser(parser: DefaultJarStreamParser): JarStreamParser

    @Binds
    fun bindJarFileParser(parser: DefaultJarFileParser): JarFileParser

    @Binds
    fun bindAarFileParser(parser: DefaultAarFileParser): AarFileParser

    @Binds
    fun bindProguardFileParser(parser: DefaultProguardFileParser): ProguardFileParser
}