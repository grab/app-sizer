package com.grab.tools

import com.grab.tools.analyzer.Analyzer
import com.grab.tools.di.DaggerAnalyzerComponent
import com.grab.tools.analyzer.ProjectInfoProvider
import com.grab.tools.utils.InputFileProvider
import com.grab.tools.utils.Logger

class AnalyzerFactory {
    fun create(
        inputFileProvider: InputFileProvider,
        projectInfoProvider: ProjectInfoProvider,
        libName: String?,
        logger: Logger
    ): Map<AnalyticsOption, Analyzer> = DaggerAnalyzerComponent.factory()
        .create(
            inputFileProvider,
            projectInfoProvider,
            libName,
            logger
        ).analyzerMap()
}