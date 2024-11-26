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

package com.grab.sizer

import com.grab.sizer.config.Config
import com.grab.sizer.config.KeySigning
import java.io.File


interface ApkGenerator {
    /**
     * Each device spec will generate a set of APKs and will return one directory per each device spec
     * @param deviceSpecs: the list of files that contain the device specs
     */
    fun generate(deviceSpecs: List<File>): List<File>
}

class DefaultApkGenerator(
    private val bundleTool: File,
    private val appBundle: File,
    private val outPutDirectory: File,
    private val signing: KeySigning?,
    private val commandExecutor: CommandExecutor = CommandExecutor()
) : ApkGenerator {
    override fun generate(deviceSpecs: List<File>): List<File> {
        if (!outPutDirectory.exists()) outPutDirectory.mkdirs()
        return deviceSpecs.map { deviceSpec ->
            File(outPutDirectory, deviceSpec.nameWithoutExtension).apply {
                if (!exists()) {
                    mkdirs()
                } else {
                    clearDirectory()
                }
            }.also { outputDir ->
                File.createTempFile(deviceSpec.nameWithoutExtension, ".apks").also { tempFile ->
                    try {
                        generateApksFile(tempFile, deviceSpec)
                        extractApksToDirectory(tempFile, deviceSpec.path, outputDir)
                    } finally {
                        tempFile.delete()
                    }
                }
            }
        }.toList()
    }

    private fun extractApksToDirectory(apksTempFile: File, deviceSpec: String, outputDirectory: File) {
        commandExecutor.execute(
            listOf(
                "java",
                "-jar",
                bundleTool.path,
                "extract-apks",
                "--apks=${apksTempFile.path}",
                "--output-dir=${outputDirectory.path}",
                "--device-spec=${deviceSpec}",
            )
        )
    }

    private fun File.clearDirectory() {
        if (!exists()) return
        if (!isDirectory) throw RuntimeException("The ${this.path} file is not a directory")
        walk().forEach { apk ->
            apk.delete()
        }
    }

    private fun generateApksFile(output: File, deviceSpec: File) {
        val signingParam = signing?.run {
            listOf(
                "--ks=${signing.keystoreFile}",
                "--ks-pass=pass:${signing.keystorePw}",
                "--key-pass=pass:${signing.keyPw}",
                "--ks-key-alias=${signing.keyAlias}",
            )
        } ?: listOf("--local-testing")

        commandExecutor.execute(
            listOf(
                "java",
                "-jar",
                bundleTool.path,
                "build-apks",
                "--bundle=${appBundle.path}",
                "--output=${output.path}",
                "--device-spec=${deviceSpec.path}",
                "--overwrite"
            ) + signingParam
        )
    }

    companion object {
        fun create(config: Config): DefaultApkGenerator = DefaultApkGenerator(
            config.apkGeneration.bundleTool,
            config.apkGeneration.appBundleFile,
            outPutDirectory = File(config.report.outputDirectory, "apks"),
            config.apkGeneration.keySigning
        )
    }
}

class CommandExecutor {
    fun execute(command: List<String>): Int = ProcessBuilder(command)
        .directory(File(System.getProperty("user.dir")))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor()
}