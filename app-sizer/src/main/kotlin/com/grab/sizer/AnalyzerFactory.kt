package com.grab.sizer

import com.grab.sizer.analyzer.Analyzer
import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.di.DaggerAnalyzerComponent
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider

class AnalyzerFactory {
    fun create(
        inputProvider: InputProvider,
        outputProvider: OutputProvider,
        projectInfoProvider: ProjectInfoProvider,
        libName: String?,
        logger: Logger
    ): Map<AnalyticsOption, Analyzer> = DaggerAnalyzerComponent.factory()
        .create(
            inputProvider,
            outputProvider,
            projectInfoProvider,
            libName,
            logger
        ).analyzerMap()
}