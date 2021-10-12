package com.grab.tools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.grab.tools.di.DaggerAppComponent
import com.grab.tools.log.log
import java.io.File


class AnalyzerCommand : CliktCommand() {

    private val apkDirs: File by option(
        "-a",
        "--apk-dir",
        help = "Path to the input apks directory"
    ).convert { File(it) }.required()

    private val librariesDir: File? by option(
        "-l",
        "--lib-dir",
        help = "Path to the directory contains all libs (aar & jar) files"
    ).convert { File(it) }

    private val projectDir: File? by option(
        "-p",
        "--project-dir",
        help = "Path to the project's root folder"
    ).convert { File(it) }

    private val outputFile: File by option(
        "-o",
        "--output-file",
        help = "Path to the output excel file"
    ).convert { File(it) }.required()

    private val mappingFile: File by option(
        "-m",
        "--mapping-file",
        help = "Path to the R8/Proguard mapping file"
    ).convert { File(it) }.required()

    private val featureMappingFile: File? by option(
        "-f",
        "--feature-mapping-file",
        help = "An yml file to grouped the feature's modules"
    ).convert { File(it) }

    private val deviceName: String? by option(
        "-d",
        "--device-name",
        help = "The device name in the device spec that we generate the APK from the app bundle"
    )

    private val extraTag: String? by option(
        "-t",
        "--tag-value",
        help = "A tag value send along with the report"
    )

    private val reportOption by option()
        .switch(
            "--libraries" to AnalyticsOption.LIBRARIES_ANALYTICS,
            "--modules" to AnalyticsOption.MODULE_ANALYTICS,
            "--apk" to AnalyticsOption.APK_ANALYTICS,
            "--basic" to AnalyticsOption.BASIC_APK_ANALYTICS,
            "--general" to AnalyticsOption.GENERAL,
        ).default(AnalyticsOption.LIBRARIES_ANALYTICS)

    override fun run() {
        librariesDir?.run { log("Lib directory $librariesDir") }
        projectDir?.run { log("Project directory $projectDir") }
        featureMappingFile?.run { log("TF mapping file $featureMappingFile") }
        mappingFile.run { log("Proguard mapping file $mappingFile") }
        apkDirs.run { log("Apk directory $apkDirs") }

        val component = DaggerAppComponent.factory()
            .create(
                libsDir = librariesDir,
                rootProjectDir = projectDir,
                featureMappingFile = featureMappingFile,
                output = outputFile,
                analyticsOption = reportOption,
                deviceName = deviceName,
                extraTag = extraTag,
                proguardMappingFile = mappingFile,
                apkDirectory = apkDirs
            )
        component.analyzerMap()[reportOption]?.process()
    }
}

