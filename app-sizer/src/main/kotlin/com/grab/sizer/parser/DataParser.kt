package com.grab.sizer.parser

import com.grab.sizer.di.AppScope
import com.grab.sizer.utils.InputProvider
import javax.inject.Inject

internal interface DataParser {
    val apks: Set<ApkFileInfo>
    val libAars: Set<AarFileInfo>
    val libJars: Set<JarFileInfo>
    val moduleAars: Set<AarFileInfo>
    val moduleJars: Set<JarFileInfo>
}

internal fun DataParser.getAars() = moduleAars + libAars
internal fun DataParser.getJars() = libJars + moduleJars

@AppScope
internal class DefaultDataParser @Inject constructor(
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val jarFileParser: JarFileParser,
    private val proguardMappingProvider: ProguardMappingProvider,
    private val inputProvider: InputProvider,
) : DataParser {
    override val apks: Set<ApkFileInfo> by lazy {
        apkFileParser.parseApks(
            inputProvider.provideApkFiles(),
            proguardMappingProvider.provide()
        )
    }
    override val libAars: Set<AarFileInfo> by lazy {
        aarFileParser.parseAars(inputProvider.provideLibraryAar())
    }
    override val libJars: Set<JarFileInfo> by lazy {
        jarFileParser.parseJars(inputProvider.provideLibraryJar())
    }
    override val moduleAars: Set<AarFileInfo> by lazy {
        aarFileParser.parseAars(inputProvider.provideModuleAar())
    }
    override val moduleJars: Set<JarFileInfo> by lazy {
        jarFileParser.parseJars(inputProvider.provideModuleJar())
    }
}