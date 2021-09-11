/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.sizer

import com.grab.sizer.config.Config
import com.grab.sizer.utils.FileQuery
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.SizerInputFile
import java.io.File


internal const val EXT_AAR = "aar"
internal const val EXT_APK = "apk"
internal const val EXT_JAR = "jar"
internal const val DEFAULT_JAR_DIR = "build/libs"
internal const val GRADLE_FILE = "build.gradle"
internal const val DEFAULT_AAR_FOLDER = "/build/outputs/aar"
private const val BUILD_FOLDER = "build"

interface FileSystem {
    fun create(parent: File, path: String): File
}

class DefaultFileSystem : FileSystem {
    override fun create(parent: File, path: String): File = File(parent, path)
}

class CltInputProvider constructor(
    private val fileQuery: FileQuery,
    private val config: Config,
    private val apksDirectory: File,
    private val fileSystem: FileSystem = DefaultFileSystem()
) : InputProvider {
    override fun provideModuleAar(): Sequence<SizerInputFile> {
        return modulesSource(DEFAULT_AAR_FOLDER)
            .flatMap { fileQuery.query(it, EXT_AAR) }
            .map { file ->
                SizerInputFile(
                    tag = getModulePath(config.projectInput.projectRoot, file),
                    file = file
                )
            }
    }

    private fun getModulePath(rootDir: File, aarFile: File): String {
        return if(config.projectInput.modulesDirIsProjectRoot) {
            val aarPath = aarFile.absolutePath
            val rootPath = rootDir.absolutePath
            val buildFolderIndex = aarPath.indexOf(File.separator + BUILD_FOLDER + File.separator)

            if (buildFolderIndex != -1) {
                val modulePathFromRoot = aarPath.substring(rootPath.length, buildFolderIndex)
                val modulePath = modulePathFromRoot.trim(File.separatorChar).replace(File.separatorChar, ':')
                modulePath.ifEmpty { rootDir.name }
            }
            else{
                // If no "build" folder is found, return the file name without extension
                aarFile.nameWithoutExtension
            }

        } else {
            aarFile.nameWithoutExtension
        }
    }

    private fun modulesSource(gradleDefaultFolder: String): Sequence<File> =
        if (config.projectInput.modulesDirIsProjectRoot) {
            config.projectInput.modulesDirectory
                .queryProjectModules()
                .map { fileSystem.create(it, gradleDefaultFolder) }
                .filter { it.exists() && it.isDirectory }
        } else
            sequenceOf(config.projectInput.modulesDirectory)


    override fun provideModuleJar(): Sequence<SizerInputFile> {
        return modulesSource(DEFAULT_JAR_DIR)
            .flatMap { fileQuery.query(it, EXT_JAR) }
            .map { file ->
                SizerInputFile(
                    tag = getModulePath(config.projectInput.projectRoot, file),
                    file = file
                )
            }
    }

    override fun provideLibraryJar(): Sequence<SizerInputFile> = fileQuery.query(
        config.projectInput.librariesDirectory, EXT_JAR
    ).map { file ->
        SizerInputFile(
            tag = file.nameWithoutExtension,
            file = file
        )
    }

    override fun provideLibraryAar(): Sequence<SizerInputFile> = fileQuery.query(
        config.projectInput.librariesDirectory, EXT_AAR
    ).map { file ->
        SizerInputFile(
            tag = file.nameWithoutExtension,
            file = file
        )
    }

    override fun provideApkFiles(): Sequence<File> = fileQuery.query(apksDirectory, EXT_APK)

    override fun provideR8MappingFile(): File? = config.projectInput.r8MappingFile

    override fun provideTeamMappingFile(): File? = config.projectInput.ownerMappingFile

    override fun provideLargeFileThreshold(): Long = config.projectInput.largeFileThreshold
}


/**
 * Only enter the module folder which is:
 * - Parent is the project root folder
 * - Folder having build.gradle file
 *  Any other folder that stay the same level with build.gradle folder will be ignored
 */
internal fun File.queryProjectModules(): Sequence<File> = walk()
    .onEnter { file ->
        if (file.parentFile == this || file.listFiles().any { it.name == GRADLE_FILE }) true
        else !file.parentFile.listFiles().any { it.name == GRADLE_FILE }
    }.filter { file ->
        file.isDirectory && file.listFiles().any { it.name == GRADLE_FILE }
    }