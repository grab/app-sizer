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


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.AppSizePluginExtension
import com.grab.plugin.sizer.configuration.InfluxDBExtension
import com.grab.plugin.sizer.configuration.RetentionPolicyExtension
import com.grab.plugin.sizer.dependencies.ArchiveDependencyManager
import com.grab.plugin.sizer.dependencies.ArchiveDependencyStore
import com.grab.plugin.sizer.dependencies.VariantInput
import com.grab.plugin.sizer.dependencies.toVariantInput
import com.grab.plugin.sizer.params
import com.grab.plugin.sizer.utils.PluginInputProvider
import com.grab.plugin.sizer.utils.PluginLogger
import com.grab.plugin.sizer.utils.PluginOutputProvider
import com.grab.sizer.AnalyticsOption
import com.grab.sizer.AppSizer
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.db.DatabaseRetentionPolicy
import com.grab.sizer.report.db.InfluxDBConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
internal abstract class AppSizeAnalysisTask : DefaultTask() {

    @get:Input
    abstract val variantInput: Property<VariantInput>

    @get:Input
    abstract val customProperties: MapProperty<String, String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val archiveDepJsonFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val apkDirectories: ConfigurableFileCollection

    @get:Input
    abstract val option: Property<AnalyticsOption>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val teamMappingFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val r8MappingFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val largeFileThreshold: Property<Long>

    @get:Input
    @get:Optional
    abstract val libName: Property<String>

    @get:Input
    @get:Optional
    abstract val influxDBConfig: Property<InfluxDBConfig>

    init {
        group = "verification"
        description = "Analyzes APK size and generates reports"

        customProperties.convention(mapOf())
        largeFileThreshold.convention(10L)
    }

    @TaskAction
    fun run() {
        validateInputs()
        apkDirectories.forEach { apkDirectory ->
            val projectInfo = ProjectInfo(
                projectName = project.rootProject.name,
                versionName = variantInput.get().versionName ?: "NA",
                deviceName = apkDirectory.nameWithoutExtension,
                buildType = variantInput.get().name
            )
            val archiveDependencyStore = ArchiveDependencyManager().readFromJsonFile(archiveDepJsonFile.asFile.get())
            AppSizer(
                inputProvider = createInputProvider(archiveDependencyStore, apkDirectory),
                outputProvider = createOutputProvider(projectInfo),
                libName = libName.orNull,
                logger = PluginLogger(project),
            ).process(option.get())
        }

    }

    private fun validateInputs() {
        require(apkDirectories.files.isNotEmpty()) { "No APK directories found" }
        require(archiveDepJsonFile.get().asFile.exists()) { "Archive dependency file not found" }
    }

    private fun createInputProvider(
        archiveDependencyStore: ArchiveDependencyStore,
        apksDirectory: File,
    ) = PluginInputProvider(
        archiveDependencyStore = archiveDependencyStore,
        r8MappingFile = r8MappingFile.orNull?.asFile,
        apksDirectory = apksDirectory,
        largeFileThreshold = largeFileThreshold.get(),
        teamMappingFile = if (teamMappingFile.isPresent) teamMappingFile.asFile.get() else null
    )

    private fun createOutputProvider(
        projectInfo: ProjectInfo
    ): PluginOutputProvider =
        PluginOutputProvider(
            influxDBConfig = influxDBConfig.orNull,
            projectInfo = projectInfo,
            customProperties = customProperties.get(),
            outputFolder = outputDirectory.asFile.get()
        )

    companion object {
        fun registerTask(
            project: Project,
            variant: BaseVariant,
            pluginExtension: AppSizePluginExtension,
            apkDirectories: Provider<ListProperty<Directory>>,
            generateArchivesListTask: TaskProvider<GenerateArchivesListTask>,
        ): TaskProvider<AppSizeAnalysisTask> {
            return project.tasks.register(
                "appSizeAnalysis${variant.name.capitalize()}", AppSizeAnalysisTask::class.java
            ) {
                this.variantInput.set(variant.toVariantInput())
                this.apkDirectories.setFrom(apkDirectories)
                this.archiveDepJsonFile.set(generateArchivesListTask.map { it.archiveDepFile.get() })
                this.libName.set(project.params().libraryName())
                this.option.set(project.params().option())
                if (pluginExtension.metrics.influxDBExtension.url.isPresent) {
                    this.influxDBConfig.set(pluginExtension.metrics.influxDBExtension.toInfluxDBConfig())
                }
                this.customProperties.set(pluginExtension.metrics.customAttributes)
                if (pluginExtension.metrics.localExtension.outputDirectory.isPresent) {
                    this.outputDirectory.set(pluginExtension.metrics.localExtension.outputDirectory)
                } else {
                    this.outputDirectory.set(project.layout.buildDirectory.dir("sizer/reports/${variant.name}"))
                }

                if(pluginExtension.input.teamMappingFile.isPresent){
                    this.teamMappingFile.set(pluginExtension.input.teamMappingFile)
                }

                this.largeFileThreshold.set(pluginExtension.input.largeFileThreshold)
                if (variant.mappingFileProvider.isPresent && variant.buildType.isMinifyEnabled) {
                    this.r8MappingFile.set(variant.mappingFileProvider.get().files.first())
                }
            }
        }
    }
}

private fun InfluxDBExtension.toInfluxDBConfig(): InfluxDBConfig = InfluxDBConfig(
    dbName = if (dbName.isPresent) dbName.get() else null,
    url = url.get(),
    username = username.orNull,
    password = password.orNull,
    reportTableName = if (reportTableName.isPresent) reportTableName.get() else null,
    databaseRetentionPolicy = if (retentionPolicy.name.isPresent) retentionPolicy.toDatabaseRetentionPolicy() else null
)

private fun RetentionPolicyExtension.toDatabaseRetentionPolicy(): DatabaseRetentionPolicy = DatabaseRetentionPolicy(
    name = name.get(),
    duration = duration.get(),
    shardDuration = shardDuration.get(),
    replicationFactor = replicationFactor.get(),
    isDefault = setAsDefault.get()
)
