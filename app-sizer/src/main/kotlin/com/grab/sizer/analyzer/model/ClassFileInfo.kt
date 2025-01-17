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

import com.grab.sizer.SizeCalculationMode


/**
 * Represents a class file in the aar/jar/apk file
 *
 * @property name The name of the class.
 * @property rawSize The original, uncompressed size of the class file.
 * @property downloadSize The size of the class file in the binary that is downloadable from Google Play.
 */
data class ClassFileInfo(
    override val name: String,
    override val rawSize: Long,
    override val downloadSize: Long = 0,
    private val sizeCalculationMode: SizeCalculationMode
) : FileInfo {

    override val size: Long
        get() = when (sizeCalculationMode) {
            SizeCalculationMode.RAW -> rawSize
            SizeCalculationMode.DOWNLOADABLE -> downloadSize
        }

    override fun equals(other: Any?): Boolean {
        if (other is ClassFileInfo) return name == other.name
        return super.equals(other)
    }

    override fun hashCode(): Int = name.hashCode()
}