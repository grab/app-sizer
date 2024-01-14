package com.grab.plugin.sizer


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.dependencies.DependencyGraph
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
        val inputFileProvider = createInputProvider(dependencyGraph)
        val logger = PluginLogger(project)
        val analyzerMap = AnalyzerFactory()
            .create(
                inputFileProvider,
                createOutputProvider(),
                object : ProjectInfoProvider {
                    override fun getProjectInfo() = projectInfo.get()
                    override fun getCustomProperties(): CustomProperties = extension.metrics.customAttributes.get()
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

    private fun createInputProvider(dependencyGraph: DependencyGraph) =
        PluginInputProvider(
            dependencyGraph = dependencyGraph,
            extension = extension,
            project = project,
            variant = appSizeTaskComponent.buildVariant(),
            apksDirectory
        )

    private fun createOutputProvider(): PluginOutputProvider = PluginOutputProvider(
        extension = extension
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
                projectInfo.set(extractProjectInfo(project, variant))
                appSizeTaskComponent = rootComponent
            }
        }

        private fun extractProjectInfo(
            project: Project,
            variant: BaseVariant
        ): ProjectInfo {
            val params = project.params()
            return ProjectInfo(
                projectName = project.rootProject.name,
                versionName = variant.mergedFlavor.versionName ?: "NA",
                deviceName = params.deviceName() ?: DEFAULT_DEVICE_NAME,
                buildType = variant.name
            )
        }
    }
}
