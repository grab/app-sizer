package com.grab.plugin.size

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

private const val DEFAULT_DEVICE_SPEC = """
    {
  "supportedAbis": ["armeabi-v7a", "arm64-v8a"],
  "supportedLocales": ["en", "es"],
  "screenDensity": 480,
  "sdkVersion": 30
}
"""

internal const val DEFAULT_DEVICE_NAME = "default_device"

internal abstract class GenerateApkTask : DefaultTask() {


    @get:Input
    abstract val bundleToolPath: Property<String>

    @get:Input
    @get:Optional
    abstract val deviceSpecFilePath: Property<String?>

    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:Input
    abstract val signingConfig: Property<InternalSigningConfig>

    @get:OutputDirectory
    abstract val outputDirectory: RegularFileProperty

    @TaskAction
    fun generateApk() {
        val apksTempFile = File.createTempFile("app", ".apks")
        var tempDeviceConfigFile: File? = null

        val deviceSpec = if (deviceSpecFilePath.orNull != null) {
            deviceSpecFilePath.get()
        } else {
            tempDeviceConfigFile = File.createTempFile("device_config", ".json")
                .apply {
                    writeBytes(
                        DEFAULT_DEVICE_SPEC.toByteArray()
                    )
                }
            tempDeviceConfigFile.path
        }

        try {
            generateApksFile(apksTempFile, deviceSpec)
            emptyOutPutDirectory()
            extractApksToDirectory(apksTempFile, deviceSpec)
        } finally {
            apksTempFile.delete()
            tempDeviceConfigFile?.delete()
            project.logger.log(LogLevel.INFO, "Temp files were deleted")
        }
    }

    private fun extractApksToDirectory(apksTempFile: File, deviceSpec: String?) {
        project.exec {
            commandLine(
                "java",
                "-jar",
                bundleToolPath.get(),
                "extract-apks",
                "--apks=${apksTempFile.path}",
                "--output-dir=${outputDirectory.asFile.get().path}",
                "--device-spec=${deviceSpec}",
            )
        }
        project.logger.log(LogLevel.QUIET, "The Apks were extracted successfully")
    }

    private fun generateApksFile(apksTempFile: File, deviceSpec: String?) {
        val realSigningConfig = signingConfig.get()
        project.exec {
            commandLine(
                "java",
                "-jar",
                bundleToolPath.get(),
                "build-apks",
                "--bundle=${bundleFile.asFile.get().path}",
                "--output=${apksTempFile.path}",
                "--ks=${realSigningConfig.storeFile}",
                "--ks-pass=pass:${realSigningConfig.storePassword}",
                "--ks-key-alias=${realSigningConfig.keyAlias}",
                "--device-spec=${deviceSpec}",
                "--overwrite"
            )
        }
        project.logger.log(LogLevel.QUIET, "The app.apks generated successfully")
    }

    private fun emptyOutPutDirectory() {
        outputDirectory.asFile.get()
            .listFiles()
            ?.forEach { apk ->
                apk.delete()
            }
    }

    companion object {
        fun registerTask(
            project: Project,
            extension: AppSizePluginExtension,
            variant: ApplicationVariant,
            apkDirectory: File
        ): TaskProvider<GenerateApkTask> {
            return project.tasks.register("generateApkFor${variant.name.capitalize()}", GenerateApkTask::class.java) {
                dependsOn("bundle${variant.name.capitalize()}")
                deviceSpecFilePath.set(project.params().deviceSpec())
                bundleToolPath.set(extension.bundleToolPath.get())
                outputDirectory.set(apkDirectory)
                bundleFile.set(project.file(extension.bundleFilePath))
                signingConfig.set(variant.signingConfig.toInternalSigningConfig())
            }
        }
    }
}

private fun SigningConfig.toInternalSigningConfig(): InternalSigningConfig = InternalSigningConfig(
    storeFile = storeFile?.path ?: "",
    storePassword = storePassword ?: "",
    keyAlias = keyAlias ?: ""
)

internal data class InternalSigningConfig(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String
) : java.io.Serializable