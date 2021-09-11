package com.grab.bundle

import com.android.tools.build.bundletool.model.AppBundle
import com.android.tools.build.bundletool.model.BundleModule
import com.android.tools.build.bundletool.model.ModuleEntry
import com.android.tools.build.bundletool.model.ZipPath
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import java.io.File
import java.util.zip.ZipFile
import kotlin.streams.toList

internal interface BundleParser {
    fun parse(bundleFile: File): Map<FileType, List<RawFileInfo>>
}

internal class BundleParserImpl : BundleParser {
    override fun parse(bundleFile: File): Map<FileType, List<RawFileInfo>> {
        val dex = DexBackedDexFile.fromInputStream()
        ZipFile(bundleFile).use { zipFile ->
            val appBundle: AppBundle = AppBundle.buildFromZip(zipFile)
            val baseModule = appBundle.baseModule
            return mutableMapOf<FileType, List<RawFileInfo>>().apply {
                setOf(BundleModule.RESOURCES_DIRECTORY, BundleModule.LIB_DIRECTORY, BundleModule.ASSETS_DIRECTORY)
                    .map { path ->
                        val type = RawFileInfo.toType(path.toString())
                        val items = baseModule.findEntriesUnderPath(path)
                            .map { entry: ModuleEntry ->
                                RawFileInfo(
                                    size = moduleEntrySize(zipFile, baseModule, entry),
                                    path = entry.path.fileName.toString(),
                                )
                            }.distinct()
                            .toList()
                        put(type, items)
                    }
            }

        }
    }

    private fun moduleEntrySize(bundle: ZipFile, module: BundleModule, entry: ModuleEntry): Long {
        return bundle.getEntry(moduleEntryFullPath(module, entry)).compressedSize
    }

    private fun moduleEntryFullPath(module: BundleModule, entry: ModuleEntry): String? {
        return ZipPath.create(module.name.toString()).resolve(entry.path).toString()
    }
}

