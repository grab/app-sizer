package com.grab.plugin.sizer.tasks

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.grab.plugin.sizer.dependencies.*

private const val NAME = "name"
private const val TYPE = "type"
private const val TYPE_MODULE = "module"
private const val TYPE_MODULE_JAVA = "java"
private const val TYPE_EXTERNAL = "external"
private const val TYPE_APP = "app"

private const val PATH_TO_ARTIFACT = "pathToArtifact"

class ArchiveDependencyTypeAdapter : TypeAdapter<ArchiveDependency>() {
    override fun write(writer: JsonWriter, dependency: ArchiveDependency) {
        writer.beginObject()
        writer.name(NAME)
        writer.value(dependency.name)
        writer.name(PATH_TO_ARTIFACT)
        writer.value(dependency.pathToArtifact)
        writer.name(TYPE)
        when (dependency) {
            is ModuleDependency -> writer.value(TYPE_MODULE)
            is JavaModuleDependency -> writer.value(TYPE_MODULE_JAVA)
            is ExternalDependency -> writer.value(TYPE_EXTERNAL)
            is AppDependency -> writer.value(TYPE_APP)
        }
        writer.endObject()
    }

    override fun read(reader: JsonReader): ArchiveDependency {
        var name = ""
        var path = ""
        var type = ""
        reader.beginObject()
        var fieldname: String? = null
        while (reader.hasNext()) {
            val token = reader.peek()
            if (token.equals(JsonToken.NAME)) {
                //get the current token
                fieldname = reader.nextName()
            }
            when (fieldname) {
                NAME -> {
                    reader.peek()
                    name = reader.nextString()
                }

                PATH_TO_ARTIFACT -> {
                    reader.peek()
                    path = reader.nextString()
                }

                TYPE -> {
                    reader.peek()
                    type = reader.nextString()
                }
            }
        }
        reader.endObject()
        return when (type) {
            TYPE_APP -> AppDependency(name, path)
            TYPE_MODULE -> ModuleDependency(name, path)
            TYPE_MODULE_JAVA -> JavaModuleDependency(name, path)
            TYPE_EXTERNAL -> ExternalDependency(name, path)
            else -> {
                throw IllegalArgumentException("The $type is not valid")
            }
        }

    }
}