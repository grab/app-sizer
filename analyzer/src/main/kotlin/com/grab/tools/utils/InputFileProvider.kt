package com.grab.tools.utils

import java.io.File

interface InputFileProvider {
    fun provideModuleAar(): Sequence<File>
    fun provideModuleJar(): Sequence<File>
    fun provideLibraryJar(): Sequence<File>
    fun provideLibraryAar(): Sequence<File>
    fun provideApkFiles(): Sequence<File>
    fun provideOutPutDirectory() : File
    fun provideR8MappingFile() : File?
    fun provideFeatureMappingFile() : File?
}