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

package com.grab.plugin.sizer.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.FinalizeBundleTask
import com.android.builder.model.SigningConfig
import com.grab.plugin.sizer.AppSizePluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import java.io.File
import java.util.*

private const val DEFAULT_DEVICE_SPEC = """
    {
  "supportedAbis": ["armeabi-v7a", "arm64-v8a"],
  "supportedLocales": ["en", "es"],
  "screenDensity": 480,
  "sdkVersion": 30
}
"""

internal const val DEFAULT_DEVICE_NAME = "default_device"

@CacheableTask
internal abstract class GenerateApkTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val bundleToolFile: RegularFileProperty

    @get:Input
    abstract val variantName: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val deviceSpecFiles: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val appBundleFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val signingConfig: Property<InternalSigningConfig>

    @get:OutputDirectories
    abstract val outputDirectories: ListProperty<Directory>

    init {
        group = "build"
        description = "Generates APKs from App Bundle for different device specifications"

        outputDirectories.convention(
            // Add the provider to ensure the deviceSpecFiles values has set
            project.provider {
                deviceSpecFiles.map { specFile ->
                    project.layout.buildDirectory
                        .dir("sizer/apk/${variantName.get()}/${specFile.nameWithoutExtension}")
                        .get()
                }
            }
        )
    }


    

    @TaskAction
    fun generateApk() {
        deviceSpecs.forEach { deviceSpecFile ->
            File.createTempFile(deviceSpecFile.name, ".apks").also { tempFile ->
                try {
                    generateApksFile(tempFile, deviceSpecFile.path)
                    val outputDir = outputDirectories.get()
                        .find {
                            it.asFile.nameWithoutExtension == deviceSpecFile.nameWithoutExtension
                        }?.asFile
                        ?: throw IllegalArgumentException("Output folders are not match for ${deviceSpecFile.nameWithoutExtension}")
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    } else {
                        outputDir.clearDirectory()
                    }
                    extractApksToDirectory(tempFile, deviceSpecFile.path, outputDir)
                } finally {
                    tempFile.delete()
                    logger.info("Temp files were deleted")
                }
            }
        }
    }

    private val deviceSpecs: Iterable<File>
        get() = if (deviceSpecFiles.isEmpty) {
            setOf(
                File.createTempFile(DEFAULT_DEVICE_NAME, ".json")
                    .apply {
                        writeBytes(
                            DEFAULT_DEVICE_SPEC.toByteArray()
                        )
                    }
            )
        } else {
            deviceSpecFiles
        }

    private fun extractApksToDirectory(apksTempFile: File, deviceSpec: String, outputDirectory: File) {
        project.exec {
            commandLine(
                "java",
                "-jar",
                bundleToolFile.asFile.get().path,
                "extract-apks",
                "--apks=${apksTempFile.path}",
                "--output-dir=${outputDirectory.path}",
                "--device-spec=${deviceSpec}",
            )
        }
        logger.quiet("The Apks for $deviceSpec were extracted successfully")
    }

    private fun generateApksFile(apksTempFile: File, deviceSpec: String) {
        val realSigningConfig = signingConfig.get()

        project.exec {
            commandLine(
                "java",
                "-jar",
                bundleToolFile.asFile.get().path,
                "build-apks",
                "--bundle=${appBundleFile.asFile.get().path}",
                "--output=${apksTempFile.path}",
                "--ks=${realSigningConfig.storeFile}",
                "--ks-pass=pass:${realSigningConfig.storePassword}",
                "--key-pass=pass:${realSigningConfig.keyPassword}",
                "--ks-key-alias=${realSigningConfig.keyAlias}",
                "--device-spec=${deviceSpec}",
                "--overwrite"
            )
        }
        logger.quiet("The app.apks generated successfully")
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
            val bundleTask = project.tasks.named("sign${variant.name.capitalize()}Bundle")
            val task = project.tasks.register("generateApk${variant.name.capitalize()}", GenerateApkTask::class.java) {
                deviceSpecFiles.setFrom(extension.input.apk.deviceSpecs)
                bundleToolFile.set(extension.input.apk.bundleToolFile)
                appBundleFile.set(
                    bundleTask.map {
                        (it as FinalizeBundleTask).finalBundleFile.get()
                    }
                )
                signingConfig.set(variant.signingConfig.toInternalSigningConfig())
                variantName.set(variant.name)
            }
            return task
        }
    }
}

internal fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.getDefault()
    ) else it.toString()
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