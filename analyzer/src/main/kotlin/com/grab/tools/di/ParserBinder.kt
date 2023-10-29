package com.grab.tools.di

import com.grab.tools.parser.AarFileParser
import com.grab.tools.parser.DefaultAarFileParser
import com.grab.tools.parser.ApkFileParser
import com.grab.tools.parser.DefaultApkFileParser
import com.grab.tools.parser.DefaultDexFileParser
import com.grab.tools.parser.DexFileParser
import com.grab.tools.parser.DefaultJarFileParser
import com.grab.tools.parser.DefaultJarStreamParser
import com.grab.tools.parser.JarFileParser
import com.grab.tools.parser.JarStreamParser
import dagger.Binds
import dagger.Module

@Module
interface ParserBinder {
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