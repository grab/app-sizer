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

package com.grab.plugin.sizer.dependencies

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.ApplicationVariant
import com.grab.plugin.sizer.utils.capitalize
import org.gradle.api.Project

internal const val BUILD_TYPE_DEBUG = "debug"
internal const val RUNTIME_CLASSPATH_SUFFIX = "RuntimeClasspath"
private const val ANDROID_EXTENSION = "android"

/**
 * The variant of the application being analyzed, used to select matching variants of
 * Android library modules.
 */
internal data class VariantInput(
    val name: String,
    val flavorName: String,
    val buildTypeName: String,
)

/**
 * A build variant of an Android library module, reconstructed from its `android` DSL
 * (product flavor combinations x build types) and validated against the variant's
 * `RuntimeClasspath` configuration, which AGP creates for every active variant.
 */
internal data class AndroidVariantCandidate(
    val name: String,
    val flavorName: String,
    val buildTypeName: String
)

internal val AndroidVariantCandidate.runtimeClasspathName: String
    get() = "$name$RUNTIME_CLASSPATH_SUFFIX"

internal val AndroidVariantCandidate.bundleAarTaskName: String
    get() = "bundle${name.capitalize()}Aar"

/**
 * Computes the list of variants for an Android project from its DSL extension.
 * Only variants backed by an existing runtime classpath configuration are returned,
 * so variants disabled through `beforeVariants` are filtered out.
 */
internal fun Project.androidVariantCandidates(): List<AndroidVariantCandidate> {
    // LibraryExtension is used instead of CommonExtension because it is non-generic in
    // both AGP 8.x and 9.x, keeping this source compatible with both lines (candidates
    // are only computed for Android library modules)
    val android = extensions.getByName(ANDROID_EXTENSION) as LibraryExtension
    val buildTypeNames = android.buildTypes.names.toList()
    val dimensions = android.flavorDimensions.toList().ifEmpty {
        android.productFlavors.mapNotNull { it.dimension }.distinct()
    }
    val flavorsByDimension = dimensions
        .map { dimension -> android.productFlavors.filter { it.dimension == dimension }.map { it.name } }
        .filter { it.isNotEmpty() }

    val flavorCombinations: List<List<String>> =
        flavorsByDimension.fold(listOf(emptyList())) { combinations, flavors ->
            combinations.flatMap { combination -> flavors.map { combination + it } }
        }

    return flavorCombinations.flatMap { combination ->
        buildTypeNames.map { buildTypeName ->
            AndroidVariantCandidate(
                name = combination.plus(buildTypeName).toCamelCase(),
                flavorName = combination.toCamelCase(),
                buildTypeName = buildTypeName
            )
        }
    }.filter { configurations.findByName(it.runtimeClasspathName) != null }
}

private fun List<String>.toCamelCase(): String =
    mapIndexed { index, part -> if (index == 0) part else part.capitalize() }
        .joinToString(separator = "")

/**
 * Selects the variant of an Android library module matching the analyzed application
 * variant, following the same fallback rules as previous plugin versions:
 * full name, then flavor with build type fallbacks, then build type with flavor
 * fallbacks, then the debug build type.
 *
 * With [enableMatchDebugVariant] the debug build type is selected directly (module
 * archives are cheaper to build in debug), matching the flavor where possible.
 */
internal class AndroidVariantMatcher(
    private val variantInput: VariantInput,
    private val flavorMatchingFallbacks: List<String>,
    private val buildTypeMatchingFallbacks: List<String>,
    private val enableMatchDebugVariant: Boolean,
) {

    fun match(project: Project): AndroidVariantCandidate? = match(project.androidVariantCandidates())

    fun match(candidates: List<AndroidVariantCandidate>): AndroidVariantCandidate? = when {
        enableMatchDebugVariant -> findDebugVariant(candidates)
        else -> extractVariant(candidates)
    }

    private fun findDebugVariant(variants: List<AndroidVariantCandidate>): AndroidVariantCandidate? {
        val debugVariants = variants.filter { it.buildTypeName == BUILD_TYPE_DEBUG }

        debugVariants.find { it.flavorName == variantInput.flavorName }?.let { return it }

        if (debugVariants.isNotEmpty()) {
            if (debugVariants.size == 1) return debugVariants.first()
            flavorMatchingFallbacks.forEach { fallback ->
                debugVariants.find { it.flavorName == fallback }?.let { return it }
            }
        }
        return null
    }

    private fun extractVariant(variants: List<AndroidVariantCandidate>): AndroidVariantCandidate? {
        variants.find { it.name == variantInput.name }?.let { return it }

        val matchFlavor = variants.filter { it.flavorName == variantInput.flavorName }
        if (matchFlavor.isNotEmpty()) {
            matchFlavor.find { it.buildTypeName == variantInput.buildTypeName }?.let { return it }
            buildTypeMatchingFallbacks.forEach { fallback ->
                matchFlavor.find { it.buildTypeName == fallback }?.let { return it }
            }
        }

        val matchBuildType = variants.filter { it.buildTypeName == variantInput.buildTypeName }
        if (matchBuildType.isNotEmpty()) {
            if (matchBuildType.size == 1) return matchBuildType.first()
            flavorMatchingFallbacks.forEach { fallback ->
                matchBuildType.find { it.flavorName == fallback }?.let { return it }
            }
        }

        return findDebugVariantByFlavorFallback(variants)
    }

    private fun findDebugVariantByFlavorFallback(variants: List<AndroidVariantCandidate>): AndroidVariantCandidate? {
        val debugVariants = variants.filter { it.buildTypeName == BUILD_TYPE_DEBUG }
        if (debugVariants.isEmpty()) return null
        if (debugVariants.size == 1) return debugVariants.first()
        flavorMatchingFallbacks.forEach { fallback ->
            debugVariants.find { it.flavorName == fallback }?.let { return it }
        }
        return null
    }
}

/**
 * Reads the `matchingFallbacks` declared in the DSL for this variant's product flavors.
 * The first flavor (in flavor-dimension order) that declares a non-empty list wins.
 */
internal fun ApplicationVariant.flavorMatchingFallbacks(android: ApplicationExtension): List<String> =
    productFlavors.firstNotNullOfOrNull { (_, flavorName) ->
        android.productFlavors.findByName(flavorName)?.matchingFallbacks?.takeIf { it.isNotEmpty() }
    } ?: emptyList()

/**
 * Reads the `matchingFallbacks` declared in the DSL for this variant's build type.
 */
internal fun ApplicationVariant.buildTypeMatchingFallbacks(android: ApplicationExtension): List<String> =
    buildType?.let { android.buildTypes.findByName(it)?.matchingFallbacks } ?: emptyList()
