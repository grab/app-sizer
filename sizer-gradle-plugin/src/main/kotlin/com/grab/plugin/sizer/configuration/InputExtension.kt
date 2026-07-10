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

package com.grab.plugin.sizer.configuration

import com.android.build.api.variant.Variant
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

private const val DEFAULT_LARGE_FILE = 10240L // 10kb

open class InputExtension @Inject constructor(objects: ObjectFactory) {
    val apk: ApkGeneratorExtension = objects.newInstance(ApkGeneratorExtension::class.java, objects)
    val teamMappingFile: RegularFileProperty = objects.fileProperty()
    val libraryOwnershipFile: RegularFileProperty = objects.fileProperty()
    var variantFilter: Action<VariantFilter>? = null
    var largeFileThreshold: Long = DEFAULT_LARGE_FILE
    var enableMatchDebugVariant = false


    fun variantFilter(action: Action<VariantFilter>) {
        variantFilter = action
    }

    fun apk(action: Action<in ApkGeneratorExtension>) {
        action.execute(apk)
    }

    fun apk(block: ApkGeneratorExtension.() -> Unit) {
        block(apk)
    }
}

/**
 * Filter used to exclude build variants from the app size analysis.
 *
 * The values mirror the app variant being configured, as reported by
 * [com.android.build.api.variant.Variant]:
 * - [name] is the full variant name, e.g. "proRelease"
 * - [buildType] is the variant's build type name, e.g. "debug" or "release"
 * - [flavors] holds the variant's product flavor names, one per flavor dimension, e.g. ["pro"]
 */
interface VariantFilter {
    fun setIgnore(ignore: Boolean)
    val buildType: String
    val flavors: List<String>
    val name: String
}

internal class DefaultVariantFilter(variant: Variant) : VariantFilter {
    var ignored: Boolean = false
    override fun setIgnore(ignore: Boolean) {
        ignored = ignore
    }

    override val buildType: String = variant.buildType.orEmpty()
    override val flavors: List<String> = variant.productFlavors.map { it.second }
    override val name: String = variant.name
}