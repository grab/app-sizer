package com.grab.tools.utils

import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val APP_MODULE = "app"
private const val JAR_EXTENSION = "jar"
private const val DEFAULT_JAR_DIR = "build/libs"
private const val GRADLE_FILE = "build.gradle"


interface JarFileQuery {
    fun query(dir: File): Sequence<File>
}

class DefaultJarFileQuery @Inject constructor(private val fileQuery: FileQuery) : JarFileQuery {
    override fun query(dir: File): Sequence<File> = fileQuery.query(dir, JAR_EXTENSION)
}

class ModuleJarFileQuery @Inject constructor(private val fileQuery: FileQuery) : JarFileQuery {
    override fun query(dir: File): Sequence<File> {
        if (dir.isFile) throw IOException("${dir.path} is not a directory")
        return dir.queryModules()
            .map { File(it, DEFAULT_JAR_DIR) }
            .filter { it.exists() && it.isDirectory }
            .flatMap {
                fileQuery.query(it, JAR_EXTENSION)
            } + appModule(dir)
    }

    private fun appModule(dir: File): Sequence<File> = fileQuery.query(File(dir, APP_MODULE), JAR_EXTENSION)
}

internal fun File.queryModules(): Sequence<File> = walk()
    .onEnter { file ->
        if (file.parentFile == this || file.listFiles().any { it.name == GRADLE_FILE }) true
        else !file.parentFile.listFiles().any { it.name == GRADLE_FILE }
    }.filter { file ->
        file.isDirectory && file.listFiles().any { it.name == GRADLE_FILE }
    }