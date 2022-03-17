package com.grab.tools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.tools.di.DaggerAppComponent
import com.grab.tools.log.log
import java.io.File


class AnalyzerCommand : CliktCommand() {

    private val settingFile: File by option(
        "-s",
        "--setting-file",
        help = "Path to the config file"
    ).convert { File(it) }.required()

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
            "--libraries" to AnalyticsOption.LIBRARIES_ANALYTICS,
            "--modules" to AnalyticsOption.MODULE_ANALYTICS,
            "--apk" to AnalyticsOption.APK_ANALYTICS,
            "--basic" to AnalyticsOption.BASIC_APK_ANALYTICS,
            "--general" to AnalyticsOption.GENERAL,
            "--large-files" to AnalyticsOption.LARGE_FILE,
            "--lib-content" to AnalyticsOption.LIB_CONTENT,
        ).default(AnalyticsOption.LIBRARIES_ANALYTICS)

    private fun validateCommand(){
        if(reportOption == AnalyticsOption.LIB_CONTENT && libName == null){
            throw IllegalArgumentException("You have to pass the --lib-name to execute this option")
        }
    }

    override fun run() {
        val settings = SettingYmlLoader().load(settingFile)
        log("Lib directory ${settings.libraryDirectoryPath}")
        log("Project directory ${settings.projectDirectoryPath}")
        log("Feature mapping file ${settings.featureMappingFilePath}")
        log("Proguard mapping file ${settings.mappingFilePath}")
        log("Apk directory ${settings.apkDirectoryPath}")
        validateCommand()
        val component = DaggerAppComponent.factory()
            .create(
                libsDir = settings.libraryDirectory,
                rootProjectDir = settings.projectDirectory,
                featureMappingFile = settings.featureMappingFile,
                output = settings.outputFile,
                analyticsOption = reportOption,
                deviceName = deviceName,
                extraTag = extraTag,
                proguardMappingFile = settings.mappingFile,
                apkDirectory = settings.apkDirectory,
                libName = libName,
                projectName = settings.projectName
            )
        component.analyzerMap()[reportOption]?.process()
    }
}

