package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo
import java.io.File
import javax.inject.Inject

private const val RESOURCE_VERSION_EXTENSION = "-v\\d\\d"

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on resource.
 */
internal class ResourceComponentMapper @Inject constructor() : ComponentMapper {
    override fun analyze(
        apks: Set<ApkFileInfo>,
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentMapperResult {
        val apkResource = apks.flatMap { it.resources }
        val aarsToResMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.resources.forEach { file -> put(file, aar.path) }
            }
        }
        val noOwnerResources = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkResource.forEach { resource ->
                val aarName = aarsToResMap[resource] ?: aarsToResMap[resource.tryOriginalFile()]
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

    private fun RawFileInfo.tryOriginalFile(): RawFileInfo {
        when {
            path.contains("$") -> {
                /** There are cases the resources files are renamed, not sure why and how.
                 * Here is an example: "/res/drawable/$bg_network_error__0.xml"
                 */
                val newPath = path.replace("$", "")
                return copy(path = newPath.removeRange(newPath.lastIndexOf("__"), newPath.lastIndexOf('.')))
            }
            path.contains(Regex(RESOURCE_VERSION_EXTENSION)) -> {
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
        }
        return this
    }
}