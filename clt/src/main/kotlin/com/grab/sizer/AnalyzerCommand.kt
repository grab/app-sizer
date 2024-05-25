package com.grab.sizer

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.sizer.config.Config
import com.grab.sizer.config.ConfigYmlLoader
import com.grab.sizer.utils.CltLogger
import com.grab.sizer.utils.DefaultFileQuery
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.ProjectInfoProviderImpl
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
        val logger: Logger = CltLogger()
        DefaultApkGenerator.create(config)
            .generate(config.apkGeneration.deviceSpecs)
            .forEach { apkDirectory ->
                AppSizer(
                    inputProvider = CltInputProvider(
                        fileQuery = DefaultFileQuery(),
                        config = config,
                        apksDirectory = apkDirectory
                    ),
                    outputProvider = CltOutputProvider(config.report),
                    projectInfoProvider = ProjectInfoProviderImpl(config, apkDirectory.nameWithoutExtension),
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

