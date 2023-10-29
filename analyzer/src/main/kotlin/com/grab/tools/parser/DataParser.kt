package com.grab.tools.parser

import com.grab.tools.di.AppScope
import com.grab.tools.utils.InputFileProvider
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
    private val inputFileProvider: InputFileProvider,
) : DataParser {
    override val apks: Set<ApkFileInfo> by lazy {
        apkFileParser.parseApks(
            inputFileProvider.provideApkFiles(),
            proguardMappingProvider.provide()
        )
    }
    override val libAars: Set<AarFileInfo> by lazy {
        aarFileParser.parseAars(inputFileProvider.provideLibraryAar())
    }
    override val libJars: Set<JarFileInfo> by lazy {
        jarFileParser.parseJars(inputFileProvider.provideLibraryJar())
    }
    override val moduleAars: Set<AarFileInfo> by lazy {
        aarFileParser.parseAars(inputFileProvider.provideModuleAar())
    }
    override val moduleJars: Set<JarFileInfo> by lazy {
        jarFileParser.parseJars(inputFileProvider.provideModuleJar())
    }
}