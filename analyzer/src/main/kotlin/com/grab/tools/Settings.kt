package com.grab.tools

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File


data class Settings(
    //Path to the input apks directory
    var apkDirectoryPath: String,
    //Path to the directory contains all libs (aar & jar) files
    var libraryDirectoryPath: String,
    //Path to the project's root folder
    var projectDirectoryPath: String,
    //Path to the output file
    var outputFilePath: String,
    // Path to the R8/Proguard mapping file
    var mappingFilePath: String,
    // A yml file to group the feature's modules
    var featureMappingFilePath: String,
    // The project name
    var projectName: String,
) {
    constructor() : this("", "", "", "", "", "", "")

    val apkDirectory: File = File(apkDirectoryPath)
    val libraryDirectory: File = File(libraryDirectoryPath)
    val projectDirectory: File = File(projectDirectoryPath)
    val outputFile: File = File(outputFilePath)
    val mappingFile: File = File(mappingFilePath)
    val featureMappingFile: File = File(featureMappingFilePath)
}

class SettingYmlLoader() {
    fun load(settingsFile: File): Settings = ObjectMapper(YAMLFactory()).run {
        registerModule(KotlinModule())
        readValue(settingsFile, Settings::class.java)
    }
}

