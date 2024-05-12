package com.grab.sizer.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

data class ApkGenerationConfig(
    @JsonProperty("bundle-tool") private val bundleToolPath: String,
    @JsonProperty("app-bundle-file") private val appBundleFilePath: String,
    @JsonProperty("device-specs") private val deviceSpecPaths: List<String>,
    @JsonProperty("key-signing") val keySigning: KeySigning?
) {

    @get:JsonIgnore
    val deviceSpecs: List<File>
        get() = deviceSpecPaths.map { File(it) }

    @get:JsonIgnore
    val bundleTool: File
        get() = File(bundleToolPath)

    @get:JsonIgnore
    val appBundleFile: File
        get() = File(appBundleFilePath)
}

data class KeySigning(
    @JsonProperty("keystore-file") val keystoreFile: String,
    @JsonProperty("keystore-pw") val keystorePw: String,
    @JsonProperty("key-alias") val keyAlias: String,
    @JsonProperty("key-pw") val keyPw: String
)