package com.grab.plugin.size


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.dependencies.DependencyExtractorImpl
import com.grab.plugin.size.dependencies.DependencyGraph
import com.grab.plugin.size.utils.PluginInputFileProvider
import com.grab.plugin.size.utils.PluginLogger
import com.grab.tools.AnalyticsOption
import com.grab.tools.AnalyzerFactory
import com.grab.tools.analyzer.report.ProjectInfo
import com.grab.tools.analyzer.ProjectInfoProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction


abstract class AppSizeAnalysisTask : DefaultTask() {

    @Internal
    lateinit var variant: BaseVariant

    @Internal
    lateinit var extension: AppSizePluginExtension

    @get:Input
    abstract val apksDirectory: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val libName: Property<String?>

    @get:Input
    @get:Optional
    abstract val option: Property<String?>

    @get:Input
    abstract val projectInfo: Property<ProjectInfo>


    @TaskAction
    fun run() {
        val extractor = DependencyExtractorImpl(project, variant)
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
        if(!option.isPresent){
            analyzerMap
                .filterKeys { it != AnalyticsOption.LIB_CONTENT && it != AnalyticsOption.LARGE_FILE }
                .forEach { (_, analyzer) -> analyzer.process() }
        }else{
            analyzerMap[AnalyticsOption.fromString(option.orNull ?: "general")]?.process()
        }
    }

    private fun createInputFileProvider(dependencyGraph: DependencyGraph) =
        PluginInputFileProvider(
            dependencyGraph = dependencyGraph,
            extension = extension,
            project = project,
            variant = variant,
            apksDirectory
        )
}
