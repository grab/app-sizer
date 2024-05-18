package com.grab.sizer.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.contains
import java.io.File

private const val DEFAULT_LARGE_FILE = 10240 // 10kb

@JsonDeserialize(using = ProjectInputConfigDeserializer::class)
data class ProjectInputConfig(
    val version: String,
    val projectName: String,
    val largeFileThreshold: Int = DEFAULT_LARGE_FILE,
    val modulesDirIsProjectRoot: Boolean,
    val librariesDirectory: File,
    val modulesDirectory: File,
    val r8MappingFile: File? = null,
    val ownerMappingFile: File? = null,
)

class ProjectInputConfigDeserializer(vc: Class<*>? = null) : StdDeserializer<ProjectInputConfig>(vc) {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ProjectInputConfig =
        jsonParser.codec.readTree<JsonNode>(jsonParser).run {
            ProjectInputConfig(
                version = get("version").asText(),
                largeFileThreshold = if (contains("large-file-threshold")) get("large-file-threshold").asInt() else DEFAULT_LARGE_FILE,
                projectName = get("project-name").asText(),
                modulesDirIsProjectRoot = get("modules-dir-is-project-root").asBoolean(),
                librariesDirectory = File(get("libraries-directory").asText()),
                modulesDirectory = File(get("modules-directory").asText()),
                r8MappingFile = if (contains("r8-mapping-file")) File(get("r8-mapping-file").asText()) else null,
                ownerMappingFile = if (contains("owner-mapping-file")) File(get("owner-mapping-file").asText()) else null,
            )
        }
}