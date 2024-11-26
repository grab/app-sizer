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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.sizer.config.Config
import com.grab.sizer.config.ConfigYmlLoader
import com.grab.sizer.utils.CliLogger
import com.grab.sizer.utils.DefaultFileQuery
import com.grab.sizer.utils.Logger
import java.io.File


class AnalyzerCommand : CliktCommand() {
    private val settingFile: File by option(
        "-s",
        "--config-file",
        help = "Path to the config file"
    ).convert { File(it) }.required()

    private val libName: String? by option(
        "-l",
        "--lib-name",
        help = """
            Name of the lib/module you want to list the content contributed to the apks
            Note that this param only necessary for the AnalyticsOption.LIB_CONTENT option
        """.trimIndent()
    )


    private val reportOption by option()
        .switch(
            "--libraries" to AnalyticsOption.LIBRARIES,
            "--modules" to AnalyticsOption.MODULES,
            "--apk" to AnalyticsOption.APK,
            "--basic" to AnalyticsOption.BASIC,
            "--codebase" to AnalyticsOption.CODEBASE,
            "--large-files" to AnalyticsOption.LARGE_FILE,
            "--lib-content" to AnalyticsOption.LIB_CONTENT,
        ).default(AnalyticsOption.DEFAULT)

    override fun run() {
        val config = ConfigYmlLoader().load(settingFile)
            .also {
                it.validateInput()
            }
        val logger: Logger = CliLogger()
        DefaultApkGenerator.create(config)
            .generate(config.apkGeneration.deviceSpecs)
            .forEach { apkDirectory ->
                AppSizer(
                    inputProvider = CliInputProvider(
                        fileQuery = DefaultFileQuery(),
                        config = config,
                        apksDirectory = apkDirectory
                    ),
                    outputProvider = CliOutputProvider(config, apkDirectory.nameWithoutExtension),
                    libName = libName,
                    logger = logger
                ).process(reportOption)
            }
    }

    private fun Config.validateInput() {
        if (reportOption == AnalyticsOption.LIB_CONTENT && libName == null) {
            throw IllegalArgumentException("You have to pass the --lib-name to execute this option")
        }
    }
}

