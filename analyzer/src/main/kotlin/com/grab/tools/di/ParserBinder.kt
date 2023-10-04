package com.grab.tools.di

import com.grab.tools.aar.AarFileParser
import com.grab.tools.aar.DefaultAarFileParser
import com.grab.tools.apk.ApkFileParser
import com.grab.tools.apk.DefaultApkFileParser
import com.grab.tools.apk.DefaultDexFileParser
import com.grab.tools.apk.DexFileParser
import com.grab.tools.jar.DefaultJarFileParser
import com.grab.tools.jar.DefaultJarStreamParser
import com.grab.tools.jar.JarFileParser
import com.grab.tools.jar.JarStreamParser
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