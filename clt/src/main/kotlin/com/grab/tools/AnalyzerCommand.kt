package com.grab.tools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.tools.utils.*
import java.io.File


class AnalyzerCommand : CliktCommand() {

    private val settingFile: File by option(
        "-s",
        "--setting-file",
        help = "Path to the config file"
    ).convert { File(it) }.required()

    private val version: String by option(
        "-v",
        "--app-version"
    ).default("0.0.0")

    private val deviceName: String by option(
        "-d",
        "--device-name",
        help = "The device name in the device spec that we generate the APK from the app bundle"
    ).required()

    private val extraTag: String by option(
        "-t",
        "--tag",
        help = "A tag value send along with the report"
    ).required()

    private val libName: String? by option(
        "-l",
        "--lib-name",
        help = "Name of the lib/module you want to list the content contributed to the apks"
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
        ).default(AnalyticsOption.CODEBASE)

    private fun validateCommand() {
        if (reportOption == AnalyticsOption.LIB_CONTENT && libName == null) {
            throw IllegalArgumentException("You have to pass the --lib-name to execute this option")
        }
    }

    override fun run() {
        val settings = SettingYmlLoader().load(settingFile)
        val logger: Logger = CltLogger()
        logger.log("Lib directory ${settings.libraryDirectoryPath}")
        logger.log("Project directory ${settings.projectDirectoryPath}")
        logger.log("Feature mapping file ${settings.featureMappingFilePath}")
        logger.log("Proguard mapping file ${settings.mappingFilePath}")
        logger.log("Apk directory ${settings.apkDirectoryPath}")
        validateCommand()

        val projectInfoProvider = ProjectInfoProviderImpl(
            projectName = settings.projectName,
            deviceName = deviceName,
            pipelineId = extraTag,
            versionName = version,
            buildType = "production",
            tag = extraTag
        )

        val inputFileProvider = CltInputFileProvider(
            fileQuery = DefaultFileQuery(),
            libsDir = settings.libraryDirectory,
            rootProjectDir = settings.projectDirectory,
            apkDirectory = settings.apkDirectory,
            outputDirectory = settings.outputFile,
            r8MappingFile = settings.mappingFile,
            ymlFeatureMappingFile = settings.featureMappingFile
        )
        AnalyzerFactory()
            .create(
                inputFileProvider,
                projectInfoProvider,
                libName,
                logger
            )[reportOption]?.process()
    }
}

