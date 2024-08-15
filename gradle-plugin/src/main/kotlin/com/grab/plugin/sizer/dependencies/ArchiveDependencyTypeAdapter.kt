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

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.grab.plugin.sizer.tasks.*

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