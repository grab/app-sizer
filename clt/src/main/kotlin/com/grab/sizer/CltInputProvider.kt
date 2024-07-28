package com.grab.sizer

import com.grab.sizer.config.Config
import com.grab.sizer.utils.FileQuery
import com.grab.sizer.utils.InputProvider
import java.io.File


internal const val EXT_AAR = "aar"
internal const val EXT_APK = "apk"
internal const val EXT_JAR = "jar"
internal const val DEFAULT_JAR_DIR = "build/libs"
internal const val GRADLE_FILE = "build.gradle"
internal const val DEFAULT_AAR_FOLDER = "/build/outputs/aar"

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
    override fun provideModuleAar(): Sequence<File> {
        return modulesSource(DEFAULT_AAR_FOLDER)
            .flatMap { fileQuery.query(it, EXT_AAR) }
    }

    private fun modulesSource(gradleDefaultFolder: String): Sequence<File> =
        if (config.projectInput.modulesDirIsProjectRoot) {
            config.projectInput.modulesDirectory
                .queryProjectModules()
                .map { fileSystem.create(it, gradleDefaultFolder) }
                .filter { it.exists() && it.isDirectory }
        } else
            sequenceOf(config.projectInput.modulesDirectory)


    override fun provideModuleJar(): Sequence<File> {
        return modulesSource(DEFAULT_JAR_DIR)
            .flatMap { fileQuery.query(it, EXT_JAR) }
    }

    override fun provideLibraryJar(): Sequence<File> = fileQuery.query(
        config.projectInput.librariesDirectory, EXT_JAR
    )

    override fun provideLibraryAar(): Sequence<File> = fileQuery.query(
        config.projectInput.librariesDirectory, EXT_AAR
    )

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