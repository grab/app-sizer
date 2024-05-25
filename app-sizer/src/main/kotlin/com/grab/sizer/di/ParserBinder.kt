package com.grab.sizer.di

import com.grab.sizer.parser.AarFileParser
import com.grab.sizer.parser.DefaultAarFileParser
import com.grab.sizer.parser.ApkFileParser
import com.grab.sizer.parser.DefaultApkFileParser
import com.grab.sizer.parser.DefaultDexFileParser
import com.grab.sizer.parser.DexFileParser
import com.grab.sizer.parser.DefaultJarFileParser
import com.grab.sizer.parser.DefaultJarStreamParser
import com.grab.sizer.parser.JarFileParser
import com.grab.sizer.parser.JarStreamParser
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
}