package com.grab.tools.utils

import com.grab.tools.DefaultFileQuery
import com.grab.tools.FileQuery
import shadow.bundletool.com.android.SdkConstants
import java.io.File
import java.io.IOException

interface AarQuery {
    fun query(dir: File): Sequence<File>
}

class DefaultAarQuery(private val fileQuery: FileQuery = DefaultFileQuery()) : AarQuery {
    override fun query(dir: File): Sequence<File> = fileQuery.query(dir, SdkConstants.EXT_AAR)
}

private const val DEFAULT_AAR_FOLDER = "build/outputs/aar"

class ModuleAarQuery(private val fileQuery: FileQuery = DefaultFileQuery()) : AarQuery {
    override fun query(dir: File): Sequence<File> {
        if (dir.isFile) throw IOException("${dir.path} is not a directory")
        return dir.queryModules()
            .map { File(it, DEFAULT_AAR_FOLDER) }
            .filter { it.exists() && it.isDirectory }
            .flatMap { fileQuery.query(it, SdkConstants.EXT_AAR) }
    }
}

fun main() {
    val jarFileQuery = ModuleAarQuery()
    jarFileQuery.query(File("/Users/van.minh/Projects/pax-android-v2")).toList()
        .forEach {
            println(it.path)
        }
}