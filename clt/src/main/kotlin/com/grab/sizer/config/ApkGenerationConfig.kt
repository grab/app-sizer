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

package com.grab.sizer.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

data class ApkGenerationConfig(
    @JsonProperty("bundle-tool") private val bundleToolPath: String,
    @JsonProperty("app-bundle-file") private val appBundleFilePath: String,
    @JsonProperty("device-specs") private val deviceSpecPaths: List<String>,
    @JsonProperty("key-signing") val keySigning: KeySigning?
) {

    @get:JsonIgnore
    val deviceSpecs: List<File>
        get() = deviceSpecPaths.map { File(it) }

    @get:JsonIgnore
    val bundleTool: File
        get() = File(bundleToolPath)

    @get:JsonIgnore
    val appBundleFile: File
        get() = File(appBundleFilePath)
}

data class KeySigning(
    @JsonProperty("keystore-file") val keystoreFile: String,
    @JsonProperty("keystore-pw") val keystorePw: String,
    @JsonProperty("key-alias") val keyAlias: String,
    @JsonProperty("key-pw") val keyPw: String
)