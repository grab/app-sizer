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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File


internal abstract class AppSizeAnalysisTask : DefaultTask() {

    @get:Input
    abstract val variantInput: Property<VariantInput>

    @get:Input
    abstract val customProperties: MapProperty<String, String>

    @get:InputFile
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
    abstract val teamMappingFile: RegularFileProperty

    @get:InputFile
    @get:Optional
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


    @TaskAction
    fun run() {
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
            generateApkTask: TaskProvider<GenerateApkTask>,
            generateArchivesListTask: TaskProvider<GenerateArchivesListTask>,
        ): TaskProvider<AppSizeAnalysisTask> {
            return project.tasks.register(
                "appSizeAnalysis${variant.name.capitalize()}", AppSizeAnalysisTask::class.java
            ) {
                this.variantInput.set(variant.toVariantInput())
                this.apkDirectories.setFrom(generateApkTask.map { it.outputDirectories })
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

                this.teamMappingFile.set(pluginExtension.input.teamMappingFile)
                this.largeFileThreshold.set(pluginExtension.input.largeFileThreshold)
                if (variant.mappingFileProvider.isPresent) {
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
