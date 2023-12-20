package com.grab.plugin.size


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.dependencies.DependencyGraph
import com.grab.plugin.size.utils.PluginInputFileProvider
import com.grab.plugin.size.utils.PluginLogger
import com.grab.tools.AnalyticsOption
import com.grab.tools.AnalyzerFactory
import com.grab.tools.analyzer.ProjectInfoProvider
import com.grab.tools.analyzer.report.ProjectInfo
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File


internal abstract class AppSizeAnalysisTask : DefaultTask() {

    @Internal
    lateinit var appSizeTaskComponent: AppSizeTaskComponent

    @Internal
    lateinit var extension: AppSizePluginExtension

    @get:Input
    abstract val apksDirectory: RegularFileProperty

    @get:Input
    abstract val option: Property<AnalyticsOption>

    @get:Input
    @get:Optional
    abstract val libName: Property<String?>

    @get:Input
    abstract val projectInfo: Property<ProjectInfo>


    @TaskAction
    fun run() {
        val extractor = appSizeTaskComponent.dependencyExtractor()
        val dependencyGraph = extractor.extract()
        val inputFileProvider = createInputFileProvider(dependencyGraph)
        val logger = PluginLogger(project)
        val analyzerMap = AnalyzerFactory()
            .create(
                inputFileProvider,
                object : ProjectInfoProvider {
                    override fun get() = projectInfo.get()
                },
                libName = libName.orNull,
                logger,
            )
        if (option.get() == AnalyticsOption.DEFAULT) {
            analyzerMap
                .filterKeys { it != AnalyticsOption.LIB_CONTENT && it != AnalyticsOption.LARGE_FILE }
                .forEach { (_, analyzer) -> analyzer.process() }
        } else {
            analyzerMap[option.get()]?.process()
        }
    }

    private fun createInputFileProvider(dependencyGraph: DependencyGraph) =
        PluginInputFileProvider(
            dependencyGraph = dependencyGraph,
            extension = extension,
            project = project,
            variant = appSizeTaskComponent.buildVariant(),
            apksDirectory
        )

    companion object {
        fun registerTask(
            project: Project,
            pluginExtension: AppSizePluginExtension,
            apkDirectory: File,
            rootComponent: AppSizeTaskComponent
        ): TaskProvider<AppSizeAnalysisTask> {
            val variant = rootComponent.buildVariant()
            return project.tasks.register(
                "appSizeAnalysis${variant.name.capitalize()}", AppSizeAnalysisTask::class.java
            ) {
                extension = pluginExtension
                apksDirectory.set(apkDirectory)
                libName.set(project.params().libraryName() as String?)
                option.set(project.params().option())
                projectInfo.set(extractProjectInfo(project, variant, pluginExtension))
                appSizeTaskComponent = rootComponent
            }
        }

        private fun extractProjectInfo(
            project: Project,
            variant: BaseVariant,
            extension: AppSizePluginExtension
        ): ProjectInfo {
            val params = project.params()
            return ProjectInfo(
                projectName = project.rootProject.name,
                versionName = variant.mergedFlavor.versionName ?: "NA",
                deviceName = params.deviceName() ?: DEFAULT_DEVICE_NAME,
                pipelineId = params.pipelineId(),
                buildType = variant.name,
                tag = extension.tag.get()
            )
        }
    }
}
