package com.grab.tools

import com.grab.tools.utils.FileQuery
import com.grab.tools.utils.InputFileProvider
import java.io.File
import java.io.IOException


private const val EXT_AAR = "aar"
private const val EXT_APK = "apk"
private const val EXT_JAR = "jar"
private const val DEFAULT_JAR_DIR = "build/libs"
private const val GRADLE_FILE = "build.gradle"
private const val DEFAULT_AAR_FOLDER = "/build/outputs/aar"


class CltInputFileProvider constructor(
    private val fileQuery: FileQuery,
    private val libsDir: File,
    private val rootProjectDir: File,
    private val apkDirectory: File,
    private val outputFile: File,
    private val r8MappingFile: File? = null,
    private val ymlFeatureMappingFile: File? = null,
) : InputFileProvider {
    override fun provideModuleAar(): Sequence<File> {
        if (rootProjectDir.isFile) throw IOException("${rootProjectDir.path} is not a directory")
        return rootProjectDir.queryModules()
            .map { File(it, DEFAULT_AAR_FOLDER) }
            .filter { it.exists() && it.isDirectory }
            .flatMap { fileQuery.query(it, EXT_AAR) }
    }

    override fun provideModuleJar(): Sequence<File> {
        if (rootProjectDir.isFile) throw IOException("${rootProjectDir.path} is not a directory")
        return rootProjectDir.queryModules()
            .map { File(it, DEFAULT_JAR_DIR) }
            .filter { it.exists() && it.isDirectory }
            .flatMap { fileQuery.query(it, EXT_JAR) }
    }

    override fun provideLibraryJar(): Sequence<File> = fileQuery.query(libsDir, EXT_JAR)

    override fun provideLibraryAar(): Sequence<File> = fileQuery.query(libsDir, EXT_AAR)

    override fun provideApkFiles(): Sequence<File> = fileQuery.query(apkDirectory, EXT_APK)

    override fun provideR8MappingFile(): File? = r8MappingFile

    override fun provideFeatureMappingFile(): File? = ymlFeatureMappingFile

    override fun provideOutPutFile(): File = outputFile
}


internal fun File.queryModules(): Sequence<File> = walk()
    .onEnter { file ->
        if (file.parentFile == this || file.listFiles().any { it.name == GRADLE_FILE }) true
        else !file.parentFile.listFiles().any { it.name == GRADLE_FILE }
    }.filter { file ->
        file.isDirectory && file.listFiles().any { it.name == GRADLE_FILE }
    }