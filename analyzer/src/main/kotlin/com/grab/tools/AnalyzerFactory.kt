package com.grab.tools

import com.grab.tools.analyzer.Analyzer
import com.grab.tools.di.DaggerAnalyzerComponent
import com.grab.tools.report.ProjectInfoProvider
import com.grab.tools.utils.InputFileProvider
import com.grab.tools.utils.Logger

class AnalyzerFactory {
    fun create(
        inputFileProvider: InputFileProvider,
        projectInfoProvider: ProjectInfoProvider,
        libName: String?,
        analyticsOption: AnalyticsOption,
        logger: Logger
    ): Analyzer = DaggerAnalyzerComponent.factory()
        .create(
            inputFileProvider,
            projectInfoProvider,
            libName,
            logger
        ).analyzerMap().getValue(analyticsOption)
}