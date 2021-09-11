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

import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo
import java.io.File
import javax.inject.Inject

private const val RESOURCE_VERSION_EXTENSION = "-v\\d\\d"

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on resource.
 */
internal class ResourceComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentMapperResult {
        val apkResource = flatMap { it.resources }
        val aarsToResMap = mutableMapOf<RawFileInfo, BinaryFileInfo>().apply {
            aars.forEach { aar ->
                aar.resources.forEach { file -> put(file, aar) }
            }
        }
        val noOwnerResources = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<BinaryFileInfo, MutableSet<RawFileInfo>>().apply {
            apkResource.forEach { resource ->
                val aarName = aarsToResMap[resource]
                    ?: aarsToResMap[resource.tryWithRemoveSpecialChar()]
                    ?: aarsToResMap[resource.tryWithRemoveVersionExtension()]
                    ?: aarsToResMap[resource.tryWithRemoveSpecialChar().tryWithRemoveVersionExtension()]
                if (aarName != null) {
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(resource)
                } else {
                    noOwnerResources.add(resource)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerResources
        )
    }

    private fun RawFileInfo.tryWithRemoveSpecialChar(): RawFileInfo {
        if (path.contains("$")) {
            /**
             * There are cases the resources files are renamed, not sure why and how.
             * Here is an example: "/res/drawable/$bg_network_error__0.xml"
             */
            val newPath = path.replace("$", "")
            return copy(path = newPath.removeRange(newPath.lastIndexOf("__"), newPath.lastIndexOf('.')))
        }
        return this
    }

    private fun RawFileInfo.tryWithRemoveVersionExtension(): RawFileInfo {
        if (path.contains(Regex(RESOURCE_VERSION_EXTENSION))) {
            /**
             * There are cases the resource directory were added with the min support sdk version
             * Ex : /res/drawable-v22/ic_geo_pickup_notes.xml
             */
            val file = File(path)
            val dir = file.parentFile
            if (dir.name.contains(Regex(RESOURCE_VERSION_EXTENSION))) {
                val dirName = dir.name
                val newDirName = dirName.removeRange(dirName.lastIndexOf('-'), dirName.length)
                val newDir = File(dir.parentFile, newDirName)
                return copy(path = File(newDir, file.name).path)
            }
        }
        return this
    }
}