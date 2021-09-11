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

package com.grab.sizer.analyzer.mapper


import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo
import javax.inject.Inject

private const val AUTO_GENERATION_LAMBDA = "-\$\$Lambda\$"
private const val AUTO_GENERATION_LAMBDA2 = "$"

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on classes.
 */
internal class ClassComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentMapperResult {
        val apkClasses = flatMap { apk -> apk.dexes }.flatMap { dex -> dex.classes }
        val libClassMap = mutableMapOf<ClassFileInfo, BinaryFileInfo>().apply {
            aars.forEach { aar ->
                aar.jars.forEach { jar ->
                    jar.classes.forEach { clazz ->
                        put(clazz, aar)
                    }
                }
            }
            jars.forEach { jar ->
                jar.classes.forEach { clazz ->
                    put(clazz, jar)
                }
            }
        }
        val noOwnerClasses = mutableSetOf<ClassFileInfo>()
        val contributors = mutableMapOf<BinaryFileInfo, MutableSet<ClassFileInfo>>().apply {
            apkClasses.forEach { clazz ->
                val lib = libClassMap[clazz] ?: libClassMap[clazz.tryOriginalClass()]
                if (lib != null) {
                    putIfAbsent(lib, mutableSetOf())
                    get(lib)?.add(clazz)
                } else {
                    noOwnerClasses.add(clazz)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerClasses
        )
    }

    private fun ClassFileInfo.tryOriginalClass(): ClassFileInfo {
        /**
         * Auto generated lambda.
         * Ex: androidx.core.widget.-$$Lambda$ContentLoadingProgressBar$aW9csiS0dCdsR2nrqov9CuXAmGo
         */
        if (name.contains(AUTO_GENERATION_LAMBDA)) {
            var newName = name.replace(AUTO_GENERATION_LAMBDA, "")
            if (newName.lastIndexOf("$") > 0)
                newName = newName.removeRange(newName.lastIndexOf("$"), newName.length)
            return copy(name = newName)
        }

        /**
         * Handle these cases
         * androidx.appcompat.app.AppCompatDelegate$$ExternalSyntheticLambda0
         * androidx.appcompat.app.AppCompatDelegateImpl$Api24Impl$$ExternalSyntheticApiModelOutline0
         */
        if(name.contains(AUTO_GENERATION_LAMBDA2)){
            return copy(name = name.substring(0, name.indexOf("$")))
        }
        return this
    }
}