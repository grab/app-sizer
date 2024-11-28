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

package com.grab.plugin.sizer.utils

import com.grab.plugin.sizer.dependencies.*
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.SizerInputFile
import java.io.File

private const val EXT_AAR = "aar"
private const val EXT_JAR = "jar"

class PluginInputProvider(
    private val archiveDependencyStore: ArchiveDependencyStore,
    private val apksDirectory: File,
    private val largeFileThreshold: Long,
    private val teamMappingFile: File? = null,
    private val r8MappingFile: File? = null,
) : InputProvider {
    override fun provideModuleAar(): Sequence<SizerInputFile> =
        archiveDependencyStore.getModuleDependency()
            .map {
                SizerInputFile(
                    tag = it.name,
                    file = File(it.pathToArtifact)
                )
            }

    override fun provideModuleJar(): Sequence<SizerInputFile> =
        archiveDependencyStore.getJavaModuleDependencies()
            .map {
                SizerInputFile(
                    tag = it.name,
                    file = File(it.pathToArtifact)
                )
            }

    override fun provideLibraryJar(): Sequence<SizerInputFile> = archiveDependencyStore.getExternalDependencies()
        .map {
            SizerInputFile(
                tag = it.name,
                file = File(it.pathToArtifact)
            )
        }
        .filter { it.file.extension.equals(EXT_JAR, true) }

    override fun provideLibraryAar(): Sequence<SizerInputFile> = archiveDependencyStore.getExternalDependencies()
        .map {
            SizerInputFile(
                tag = it.name,
                file = File(it.pathToArtifact)
            )
        }
        .filter { it.file.extension.equals(EXT_AAR, true) }

    override fun provideApkFiles(): Sequence<File> {
        return apksDirectory.listFiles()?.asSequence() ?: emptySequence()
    }

    override fun provideR8MappingFile(): File? = r8MappingFile

    override fun provideTeamMappingFile(): File? = teamMappingFile

    override fun provideLargeFileThreshold(): Long = largeFileThreshold
}


fun ArchiveDependencyStore.getExternalDependencies(): Sequence<ExternalDependency> =
    asSequence().filterIsInstance(ExternalDependency::class.java)

fun ArchiveDependencyStore.getJavaModuleDependencies(): Sequence<JavaModuleDependency> =
    asSequence().filterIsInstance(JavaModuleDependency::class.java)

fun ArchiveDependencyStore.getModuleDependency(): Sequence<ModuleDependency> =
    asSequence().filterIsInstance(ModuleDependency::class.java)

fun ArchiveDependencyStore.getApp(): AppDependency = asSequence().filterIsInstance<AppDependency>().first()