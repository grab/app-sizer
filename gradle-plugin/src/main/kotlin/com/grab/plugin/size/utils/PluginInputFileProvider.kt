package com.grab.plugin.size.utils

import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.AppSizePluginExtension
import com.grab.plugin.size.dependencies.*
import com.grab.tools.utils.InputFileProvider
import org.gradle.api.Project
import java.io.File

private const val EXT_AAR = "aar"
private const val EXT_JAR = "jar"

class PluginInputFileProvider(
    private val dependencyGraph: DependencyGraph,
    private val extension: AppSizePluginExtension,
    private val project: Project,
    private val variant: BaseVariant
) : InputFileProvider {
    override fun provideModuleAar(): Sequence<File> =
        dependencyGraph.getModuleDependency().map { File(it.pathToArtifact) }

    override fun provideModuleJar(): Sequence<File> =
        dependencyGraph.getJavaModuleDependencies().map { File(it.pathToArtifact) }

    override fun provideLibraryJar(): Sequence<File> = dependencyGraph.getExternalDependencies()
        .map { File(it.pathToArtifact) }
        .filter { it.extension.equals(EXT_JAR, true) }

    override fun provideLibraryAar(): Sequence<File> = dependencyGraph.getExternalDependencies()
        .map { File(it.pathToArtifact) }
        .filter { it.extension.equals(EXT_AAR, true) }

    override fun provideApkFiles(): Sequence<File> {
        TODO("Not yet implemented")
    }

    override fun provideOutPutFile(): File {
        TODO("Not yet implemented")
    }

    override fun provideR8MappingFile(): File? = variant.mappingFileProvider.get().files.first()

    override fun provideFeatureMappingFile(): File? = extension.featureMappingFile.asFile.get()
}

fun DependencyGraph.getExternalDependencies(): Sequence<ExternalDependency> =
    getAll().filterIsInstance(ExternalDependency::class.java)

fun DependencyGraph.getJavaModuleDependencies(): Sequence<JavaModuleDependency> =
    getAll().filterIsInstance(JavaModuleDependency::class.java)

fun DependencyGraph.getModuleDependency(): Sequence<ModuleDependency> =
    getAll().filterIsInstance(ModuleDependency::class.java)

fun DependencyGraph.getApp(): AppDependency = getAll().filterIsInstance<AppDependency>().first()