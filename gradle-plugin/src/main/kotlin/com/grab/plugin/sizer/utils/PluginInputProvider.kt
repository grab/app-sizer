package com.grab.plugin.sizer.utils

import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.dependencies.*
import com.grab.sizer.utils.InputProvider
import java.io.File

private const val EXT_AAR = "aar"
private const val EXT_JAR = "jar"

class PluginInputProvider(
    private val archiveDependencyStore: ArchiveDependencyStore,
    private val apksDirectory: File,
    private val largeFileThreshold: Int,
    private val teamMappingFile: File? = null,
    private val r8MappingFile: File? = null,
) : InputProvider {
    override fun provideModuleAar(): Sequence<File> =
        archiveDependencyStore.getModuleDependency().map { File(it.pathToArtifact) }

    override fun provideModuleJar(): Sequence<File> =
        archiveDependencyStore.getJavaModuleDependencies().map { File(it.pathToArtifact) }

    override fun provideLibraryJar(): Sequence<File> = archiveDependencyStore.getExternalDependencies()
        .map { File(it.pathToArtifact) }
        .filter { it.extension.equals(EXT_JAR, true) }

    override fun provideLibraryAar(): Sequence<File> = archiveDependencyStore.getExternalDependencies()
        .map { File(it.pathToArtifact) }
        .filter { it.extension.equals(EXT_AAR, true) }

    override fun provideApkFiles(): Sequence<File> {
        return apksDirectory.listFiles()?.asSequence() ?: emptySequence()
    }

    override fun provideR8MappingFile(): File? = r8MappingFile

    override fun provideTeamMappingFile(): File? = teamMappingFile

    override fun provideLargeFileThreshold(): Int = largeFileThreshold
}


fun ArchiveDependencyStore.getExternalDependencies(): Sequence<ExternalDependency> =
    asSequence().filterIsInstance(ExternalDependency::class.java)

fun ArchiveDependencyStore.getJavaModuleDependencies(): Sequence<JavaModuleDependency> =
    asSequence().filterIsInstance(JavaModuleDependency::class.java)

fun ArchiveDependencyStore.getModuleDependency(): Sequence<ModuleDependency> =
    asSequence().filterIsInstance(ModuleDependency::class.java)

fun ArchiveDependencyStore.getApp(): AppDependency = asSequence().filterIsInstance<AppDependency>().first()