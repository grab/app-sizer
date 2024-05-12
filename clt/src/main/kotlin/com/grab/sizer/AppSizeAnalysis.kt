package com.grab.sizer

import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider

interface AppSizeAnalysis {
    fun analysis(
        inputProvider: InputProvider,
        outputProvider: OutputProvider,
        projectInfoProvider: ProjectInfoProvider
    )
}

class DefaultAppSizeAnalysis(
    private val logger: Logger,
    private val option: AnalyticsOption,
    private val libName: String?
) : AppSizeAnalysis {
    override fun analysis(
        inputProvider: InputProvider,
        outputProvider: OutputProvider,
        projectInfoProvider: ProjectInfoProvider,
    ) {
        val analyzerMap = AnalyzerFactory()
            .create(
                inputProvider = inputProvider,
                outputProvider = outputProvider,
                projectInfoProvider = projectInfoProvider,
                libName = libName,
                logger = logger
            )
        if (option == AnalyticsOption.DEFAULT) {
            analyzerMap
                .filterKeys { it != AnalyticsOption.LIB_CONTENT }
                .forEach { (_, analyzer) -> analyzer.process() }
        } else {
            analyzerMap[option]?.process()
        }
    }
}