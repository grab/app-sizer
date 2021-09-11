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

package com.grab.sizer.analyzer.model

import java.io.File


/**
 * Specifies the type of file extracted from a jar or aar file.
 */
enum class FileType {
    RESOURCE, NATIVE_LIB, ASSET, DEX, JAR, OTHERS, CLASS
}

/**
 * Represents a file/class within a jar, aar or apk file and provides its various sizes.
 *
 * @property name The name of the file/class.
 * @property downloadSize The size of the file in the binary that is downloadable from Google Play.(zipped APKs)
 * @property size The original size of the file.
 */
interface FileInfo {
    val name: String
    val downloadSize: Long
    val size: Long
}

/**
 * Defines a raw file which is not a class in a jar, aar or apk file
 *
 * @property path             The path to the raw file within the aar/jar file.
 * @property downloadSize     The size of the file in the downloadable binary from Google Play.
 * @property size             The original, uncompressed size of the file.
 */
data class RawFileInfo(
    val path: String,
    override val downloadSize: Long,
    override val size: Long
) : FileInfo {
    val type: FileType
        get() = when {
            path.startsWith("/res/") -> FileType.RESOURCE
            path.endsWith(".so", true) -> FileType.NATIVE_LIB
            path.startsWith("/assets/") -> FileType.ASSET
            path.endsWith(".dex") -> FileType.DEX
            path.endsWith(".jar") -> FileType.JAR
            path.endsWith(".class") -> FileType.CLASS
            else -> FileType.OTHERS
        }
    override val name: String
        get() = File(path).name

    override fun equals(other: Any?): Boolean {
        if (other is RawFileInfo) return path == other.path
        return super.equals(other)
    }

    override fun hashCode(): Int = path.hashCode()
}

internal fun Set<FileInfo>.castToClass(): Set<ClassFileInfo> = filterIsInstance<ClassFileInfo>().toSet()
internal fun Set<FileInfo>.castToRawFile(): Set<RawFileInfo> = filterIsInstance<RawFileInfo>().toSet()