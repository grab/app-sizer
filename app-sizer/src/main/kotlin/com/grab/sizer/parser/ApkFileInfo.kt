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

import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

/**
 * A data class that represents the output after parse an APK file by [ApkFileParser].
 * It encapsulates the parsed data from an APK file, including information such as its name,
 * size, download size, and the set of resources, native libraries, assets, others, and dex files it contains.
 *
 * @property name The name of the APK file.
 * @property resources A set of resources contained in the APK file.
 * @property nativeLibs A set of native libraries contained in the APK file.
 * @property assets A set of assets files contained in the APK file.
 * @property others A set of FileInfo objects representing other files in the APK file.
 * @property dexes A set of [DexFileInfo] objects representing dex files in the APK file.
 */
data class ApkFileInfo(
    val name: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val dexes: Set<DexFileInfo>
)