package com.grab.plugin.sizer

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.FinalizeBundleTask
import com.android.builder.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val deviceSpecFiles: ConfigurableFileCollection

    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:Input
    abstract val signingConfig: Property<InternalSigningConfig>

    @get:Input
    abstract val outputDirectoryPath: Property<String>

    /**
     * The task will generate a set of APKs for each device specification.
     * Each of these sets will then be stored in its own distinct folder. And they all listed in outputDirectories
     */
    @get:OutputDirectories
    val outputDirectories: FileCollection
        get() {
            return project.files(apkDirectories)
        }

    private val apkDirectories = mutableListOf<File>()

    @TaskAction
    fun generateApk() {
        val deviceSpecs = if (deviceSpecFiles.isEmpty) {
            setOf(
                File.createTempFile("device_config", ".json")
                    .apply {
                        writeBytes(
                            DEFAULT_DEVICE_SPEC.toByteArray()
                        )
                    }
            )
        } else {
            deviceSpecFiles
        }

        deviceSpecs.forEach { deviceSpecFile ->
            File.createTempFile(deviceSpecFile.name, ".apks").run {
                try {
                    generateApksFile(this, deviceSpecFile.path)
                    val outputDir = File(outputDirectoryPath.get(), deviceSpecFile.name).apply {
                        if (!exists()) {
                            mkdirs()
                        } else {
                            clearDirectory()
                        }
                    }
                    extractApksToDirectory(this, deviceSpecFile.path, outputDir)
                    apkDirectories.add(outputDir)
                } finally {
                    delete()
                    project.logger.log(LogLevel.INFO, "Temp files were deleted")
                }
            }
        }
    }

    private fun extractApksToDirectory(apksTempFile: File, deviceSpec: String, outputDirectory: File) {
        project.exec {
            commandLine(
                "java",
                "-jar",
                bundleToolPath.get(),
                "extract-apks",
                "--apks=${apksTempFile.path}",
                "--output-dir=${outputDirectory.path}",
                "--device-spec=${deviceSpec}",
            )
        }
        project.logger.log(LogLevel.QUIET, "The Apks for $deviceSpec were extracted successfully")
    }

    private fun generateApksFile(apksTempFile: File, deviceSpec: String) {
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
                "--key-pass=pass:${realSigningConfig.keyPassword}",
                "--ks-key-alias=${realSigningConfig.keyAlias}",
                "--device-spec=${deviceSpec}",
                "--overwrite"
            )
        }
        project.logger.log(LogLevel.QUIET, "The app.apks generated successfully")
    }

    private fun File.clearDirectory() {
        if (!exists()) return
        if (!isDirectory) throw RuntimeException("The ${this.path} file is not a directory")
        walk().forEach { apk ->
            apk.delete()
        }
    }

    companion object {
        fun registerTask(
            project: Project,
            extension: AppSizePluginExtension,
            variant: ApplicationVariant
        ): TaskProvider<GenerateApkTask> {
            val apkDirectory = File("${variant.outputs.first().outputFile.parent}/apks")
            val bundleTask = project.tasks.named("sign${variant.name.capitalize()}Bundle")
            val task = project.tasks.register("generateApk${variant.name.capitalize()}", GenerateApkTask::class.java) {

                deviceSpecFiles.setFrom(extension.android.apk.deviceSpecs)
                bundleToolPath.set(extension.android.apk.bundleToolPath)
                outputDirectoryPath.set(apkDirectory.path)
                bundleFile.set(
                    bundleTask.map { (it as FinalizeBundleTask).finalBundleFile.get() }
                )
                signingConfig.set(variant.signingConfig.toInternalSigningConfig())
            }
            return task
        }
    }
}

private fun SigningConfig.toInternalSigningConfig(): InternalSigningConfig = InternalSigningConfig(
    storeFile = storeFile?.path ?: "",
    storePassword = storePassword ?: "",
    keyAlias = keyAlias ?: "",
    keyPassword = keyPassword ?: ""
)

internal data class InternalSigningConfig(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String
) : java.io.Serializable