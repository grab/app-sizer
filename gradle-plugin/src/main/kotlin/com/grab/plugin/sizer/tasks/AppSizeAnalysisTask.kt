package com.grab.plugin.sizer.tasks


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.AppSizePluginExtension
import com.grab.plugin.sizer.dependencies.ArchiveDependencyStore
import com.grab.plugin.sizer.params
import com.grab.plugin.sizer.utils.PluginInputProvider
import com.grab.plugin.sizer.utils.PluginLogger
import com.grab.plugin.sizer.utils.PluginOutputProvider
import com.grab.sizer.AnalyticsOption
import com.grab.sizer.AnalyzerFactory
import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File


internal abstract class AppSizeAnalysisTask : DefaultTask() {

    @Internal
    lateinit var extension: AppSizePluginExtension

    @get:Internal
    abstract val variant: Property<BaseVariant>

    @get:InputFile
    abstract val archiveDepJsonFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val apkDirectories: ConfigurableFileCollection

    @get:Input
    abstract val option: Property<AnalyticsOption>

    @get:Input
    @get:Optional
    abstract val libName: Property<String?>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty


    @TaskAction
    fun run() {
        apkDirectories.forEach { apkDirectory ->
            val projectInfo = ProjectInfo(
                projectName = project.rootProject.name,
                versionName = variant.get().mergedFlavor.versionName ?: "NA",
                deviceName = apkDirectory.nameWithoutExtension,
                buildType = variant.get().name
            )

            val archiveDependencyStore = ArchiveDependencyManager().readFromJsonFile(archiveDepJsonFile.asFile.get())

            val analyzerMap = AnalyzerFactory()
                .create(
                    inputProvider = createInputProvider(archiveDependencyStore, apkDirectory),
                    outputProvider = createOutputProvider(),
                    projectInfoProvider = object : ProjectInfoProvider {
                        override fun getProjectInfo() = projectInfo
                        override fun getCustomProperties(): CustomProperties = extension.metrics.customAttributes.get()
                    },
                    libName = libName.orNull,
                    logger = PluginLogger(project),
                )
            if (option.get() == AnalyticsOption.DEFAULT) {
                analyzerMap
                    .filterKeys { it != AnalyticsOption.LIB_CONTENT }
                    .forEach { (_, analyzer) -> analyzer.process() }
            } else {
                analyzerMap[option.get()]?.process()
            }
        }

    }

    private fun createInputProvider(
        archiveDependencyStore: ArchiveDependencyStore,
        apksDirectory: File,
    ) = PluginInputProvider(
        archiveDependencyStore = archiveDependencyStore,
        extension = extension,
        project = project,
        variant = variant.get(),
        apksDirectory
    )

    private fun createOutputProvider(): PluginOutputProvider = PluginOutputProvider(extension, outputDirectory.asFile.get())

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
                this.extension = pluginExtension
                this.variant.set(variant)
                this.apkDirectories.setFrom(generateApkTask.map { it.outputDirectories })
                // This hardcode config is specific for Grab team only. For some reason, we got the archives.json beforehand
                if(pluginExtension.forGrabTeamOnly)
                    this.archiveDepJsonFile.set(project.layout.buildDirectory.file("sizer/dep/dependencies.json"))
                else
                    this.archiveDepJsonFile.set(generateArchivesListTask.map { it.archiveDepFile.get() })

                this.libName.set(project.params().libraryName())
                this.option.set(project.params().option())
                this.outputDirectory.set(extension.metrics.localExtension.outputDirectory)
            }
        }
    }
}
