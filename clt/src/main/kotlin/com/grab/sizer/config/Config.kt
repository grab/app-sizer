package com.grab.sizer.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class Config(
    @JsonProperty("grab-team-only") val forGrabTeamOnly: Boolean,
    @JsonProperty("project-input") val projectInput: ProjectInputConfig,
    @JsonProperty("apk-generation") val apkGeneration: ApkGenerationConfig,
    @JsonProperty("report") val report: ReportConfig
)


class ConfigYmlLoader() {
    fun load(configFile: File): Config = ObjectMapper(YAMLFactory()).run {
        registerModule(
            KotlinModule.Builder()
                .build()
        )
        readValue(configFile)
    }
}

