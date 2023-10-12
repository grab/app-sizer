package com.grab.plugin.size

import com.android.builder.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class GenerateApkTask : DefaultTask() {

    @get:Input
    abstract val bundleToolPath: Property<String>

    @get:Input
    abstract val androidDeviceConfig: Property<String>

    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:Input
    abstract val signingConfig: Property<InternalSigningConfig>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generateApk() {

        val tempFile = File.createTempFile("deviceConfig", ".json").apply {
            writeText(androidDeviceConfig.get())
        }
        try {
            val realSigningConfig = signingConfig.get()
            project.exec {
                commandLine(
                    "java",
                    "-jar",
                    bundleToolPath,
                    "build-apks",
                    "--bundle=${bundleFile.asFile.get().path}",
                    "--output=${outputFile.asFile.get().path}",
                    "--ks=${realSigningConfig.storeFile}",
                    "--ks-pass=pass:${realSigningConfig.storePassword}",
                    "--ks-key-alias=${realSigningConfig.keyAlias}",
                    "--device-spec=$tempFile"
                )
            }
            project.logger.log(LogLevel.QUIET, "APK generated successfully")
        } finally {
            tempFile.delete()
        }


    }
}

fun SigningConfig.toInternalSigningConfig(): InternalSigningConfig = InternalSigningConfig(
    storeFile = storeFile?.path ?: "",
    storePassword = storePassword ?: "",
    keyAlias = keyAlias ?: ""
)

data class InternalSigningConfig(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String
) : java.io.Serializable