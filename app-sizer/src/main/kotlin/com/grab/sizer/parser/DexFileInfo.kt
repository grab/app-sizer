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
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.SizeCalculationMode

/**
 * A data class that represents a dex file parsed from the APK by [ApkFileParser].
 * It contains details like the dex file name, download size, set of class files, and normal size.
 *
 * @property name The name of the dex file.
 * @property downloadSize The download size of the dex file.
 * @property classes A set of ClassFileInfo objects representing classes contained in the dex file.
 * @property rawSize The size of the dex file.
 */
data class DexFileInfo(
    override val name: String,
    override val downloadSize: Long,
    val classes: Set<ClassFileInfo>,
    val others: Set<RawFileInfo> = emptySet(),
    override val rawSize: Long,
    private val sizeCalculationMode: SizeCalculationMode
) : FileInfo {

    override val size: Long get() = when (sizeCalculationMode) {
        SizeCalculationMode.RAW -> rawSize
        SizeCalculationMode.DOWNLOADABLE -> downloadSize
    }
    // The total size of classes and other files in the dex file (computed lazily).
    val classSize: Long by lazy { classes.sumOf { it.rawSize } + others.sumOf { it.rawSize } }
}

