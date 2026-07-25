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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AndroidVariantMatcherTest {

    private val proRelease = VariantInput(name = "proRelease", flavorName = "pro", buildTypeName = "release")

    private fun matcher(
        variantInput: VariantInput = proRelease,
        flavorMatchingFallbacks: List<String> = emptyList(),
        buildTypeMatchingFallbacks: List<String> = emptyList(),
        enableMatchDebugVariant: Boolean = false,
    ) = AndroidVariantMatcher(variantInput, flavorMatchingFallbacks, buildTypeMatchingFallbacks, enableMatchDebugVariant)

    private fun candidate(name: String, flavor: String, buildType: String) =
        AndroidVariantCandidate(name = name, flavorName = flavor, buildTypeName = buildType)

    @Test
    fun `returns the full name match when available`() {
        val match = matcher().match(
            listOf(
                candidate("proDebug", "pro", "debug"),
                candidate("proRelease", "pro", "release"),
                candidate("freeRelease", "free", "release"),
            )
        )

        assertEquals("proRelease", match?.name)
    }

    @Test
    fun `matches by flavor and build type when no full name match exists`() {
        // The variant name does not follow the <flavor><BuildType> convention here;
        // matching still succeeds because it is done on the flavor and build type fields
        val match = matcher().match(
            listOf(
                candidate("proDebug", "pro", "debug"),
                candidate("customProRelease", "pro", "release"),
            )
        )

        assertEquals("customProRelease", match?.name)
    }

    @Test
    fun `uses build type fallbacks when the flavor matches but the build type does not`() {
        val match = matcher(buildTypeMatchingFallbacks = listOf("staging")).match(
            listOf(
                candidate("proDebug", "pro", "debug"),
                candidate("proStaging", "pro", "staging"),
            )
        )

        assertEquals("proStaging", match?.name)
    }

    @Test
    fun `uses flavor fallbacks when the build type matches but the flavor does not`() {
        val match = matcher(flavorMatchingFallbacks = listOf("standard")).match(
            listOf(
                candidate("standardRelease", "standard", "release"),
                candidate("liteRelease", "lite", "release"),
                candidate("standardDebug", "standard", "debug"),
            )
        )

        assertEquals("standardRelease", match?.name)
    }

    @Test
    fun `selects the single build type match without needing fallbacks`() {
        val match = matcher().match(
            listOf(
                candidate("standardDebug", "standard", "debug"),
                candidate("standardRelease", "standard", "release"),
            )
        )

        assertEquals("standardRelease", match?.name)
    }

    @Test
    fun `falls back to the debug build type when nothing else matches`() {
        val match = matcher().match(
            listOf(candidate("debug", "", "debug"))
        )

        assertEquals("debug", match?.name)
    }

    @Test
    fun `returns null when several debug variants remain ambiguous`() {
        val match = matcher().match(
            listOf(
                candidate("standardDebug", "standard", "debug"),
                candidate("liteDebug", "lite", "debug"),
            )
        )

        assertNull(match)
    }

    @Test
    fun `enableMatchDebugVariant selects the debug build type of the same flavor`() {
        val match = matcher(enableMatchDebugVariant = true).match(
            listOf(
                candidate("proRelease", "pro", "release"),
                candidate("proDebug", "pro", "debug"),
                candidate("freeDebug", "free", "debug"),
            )
        )

        assertEquals("proDebug", match?.name)
    }

    @Test
    fun `enableMatchDebugVariant selects the only debug variant when flavors differ`() {
        val match = matcher(enableMatchDebugVariant = true).match(
            listOf(
                candidate("standardRelease", "standard", "release"),
                candidate("standardDebug", "standard", "debug"),
            )
        )

        assertEquals("standardDebug", match?.name)
    }

    @Test
    fun `enableMatchDebugVariant uses flavor fallbacks to disambiguate debug variants`() {
        val match = matcher(
            enableMatchDebugVariant = true,
            flavorMatchingFallbacks = listOf("lite"),
        ).match(
            listOf(
                candidate("standardDebug", "standard", "debug"),
                candidate("liteDebug", "lite", "debug"),
            )
        )

        assertEquals("liteDebug", match?.name)
    }

    @Test
    fun `derived names follow the runtime classpath and bundle task conventions`() {
        val candidate = candidate("proRelease", "pro", "release")

        assertEquals("proReleaseRuntimeClasspath", candidate.runtimeClasspathName)
        assertEquals("bundleProReleaseAar", candidate.bundleAarTaskName)
    }
}
