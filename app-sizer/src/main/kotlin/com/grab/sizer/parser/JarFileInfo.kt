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

package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

/**
 * A data class that represents a jar file parsed from the jar by [JarFileParser] or [JarStreamParser].
 * It contains details like the classes, path to the file, native libs and others
 */
data class JarFileInfo(
    override val name: String,
    override val path: String,
    override val tag: String,
    val classes: Set<ClassFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val others: Set<RawFileInfo> = emptySet()
) : BinaryFileInfo {
    override fun hashCode(): Int = path.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is JarFileInfo) return false
        return path == other.path
    }
}