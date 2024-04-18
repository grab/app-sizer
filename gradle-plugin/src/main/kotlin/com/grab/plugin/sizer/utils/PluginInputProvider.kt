package com.grab.plugin.sizer.utils

import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.AppSizePluginExtension
import com.grab.plugin.sizer.dependencies.*
import com.grab.sizer.utils.InputProvider
import org.gradle.api.Project
import java.io.File

private const val EXT_AAR = "aar"
private const val EXT_JAR = "jar"

class PluginInputProvider(
    private val archiveDependencyStore: ArchiveDependencyStore,
    private val extension: AppSizePluginExtension,
    private val project: Project,
    private val variant: BaseVariant,
    private val apksDirectory: File,
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

    override fun provideR8MappingFile(): File? {
        return if (variant.mappingFileProvider.isPresent) {
            variant.mappingFileProvider.get().files.first()
        } else null
    }

    override fun provideFeatureMappingFile(): File? =
        if(extension.featureMappingFile.isPresent) extension.featureMappingFile.asFile.get() else null
}

fun ArchiveDependencyStore.getExternalDependencies(): Sequence<ExternalDependency> =
    asSequence().filterIsInstance(ExternalDependency::class.java)

fun ArchiveDependencyStore.getJavaModuleDependencies(): Sequence<JavaModuleDependency> =
    asSequence().filterIsInstance(JavaModuleDependency::class.java)

fun ArchiveDependencyStore.getModuleDependency(): Sequence<ModuleDependency> =
    asSequence().filterIsInstance(ModuleDependency::class.java)

fun ArchiveDependencyStore.getApp(): AppDependency = asSequence().filterIsInstance<AppDependency>().first()