package com.grab.plugin.size


import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.dependencies.DependencyExtractorImpl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class AppSizeAnalysisTask : DefaultTask() {

    @Internal
    lateinit var variant: BaseVariant

    @TaskAction
    fun run() {
        val extractor = DependencyExtractorImpl(project, variant)
        val dependencyGraph = extractor.extract()
        println(dependencyGraph.toString())
    }
}