package com.grab.plugin.sizer.dependencies

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class ArchiveDependencyManager {
    private fun createGson(): Gson = GsonBuilder()
        .registerTypeHierarchyAdapter(ArchiveDependency::class.java, ArchiveDependencyTypeAdapter())
        .create()

    fun writeToJsonFile(archiveDependencyStore: ArchiveDependencyStore, outputFile: File) {
        outputFile.writeText(createGson().toJson(archiveDependencyStore))
    }

    fun readFromJsonFile(inputJsonFile: File): ArchiveDependencyStore {
        val hashSetType = object : TypeToken<HashSet<ArchiveDependency>>() {}.type
        return createGson().fromJson(inputJsonFile.readText(), hashSetType)
    }
}