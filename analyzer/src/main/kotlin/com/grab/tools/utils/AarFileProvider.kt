package com.grab.tools.utils

import shadow.bundletool.com.android.SdkConstants
import java.io.File
import java.io.IOException

interface AarFileProvider {
    fun provide(dir: File): Sequence<File>
}

class DefaultAarFileProvider(private val fileQuery: FileQuery = DefaultFileQuery()) : AarFileProvider {
    override fun provide(dir: File): Sequence<File> = fileQuery.query(dir, SdkConstants.EXT_AAR)
}

internal const val DEFAULT_AAR_FOLDER = "/build/outputs/aar"

class ModuleAarFileProvider(private val fileQuery: FileQuery = DefaultFileQuery()) : AarFileProvider {
    override fun provide(dir: File): Sequence<File> {
        if (dir.isFile) throw IOException("${dir.path} is not a directory")
        return dir.queryModules()
            .map { File(it, DEFAULT_AAR_FOLDER) }
            .filter { it.exists() && it.isDirectory }
            .flatMap { fileQuery.query(it, SdkConstants.EXT_AAR) }
    }
}

fun main() {
    val jarFileQuery = ModuleAarFileProvider()
    jarFileQuery.provide(File("/Users/van.minh/Projects/pax-android-v2")).toList()
        .forEach {
            println(it.path)
        }
}