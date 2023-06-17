package com.grab.plugin.size


import com.android.build.api.variant.Variant
import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.dependencies.DependencyExtractorImpl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class AppSizeAnalysisTask : DefaultTask() {

    @Input
    lateinit var variant: BaseVariant

    @TaskAction
    fun run() {
        val extractor = DependencyExtractorImpl()
        val dependencyGraph = extractor.extract(project, variant)
        println(dependencyGraph.toString())
    }
}