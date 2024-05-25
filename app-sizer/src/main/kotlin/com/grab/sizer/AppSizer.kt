package com.grab.sizer

import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.di.DaggerAnalyzerComponent
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider
import java.util.*

class AppSizer(
    private val inputProvider: InputProvider,
    private val outputProvider: OutputProvider,
    private val projectInfoProvider: ProjectInfoProvider,
    private val libName: String?,
    private val logger: Logger
) {
    fun process(option: AnalyticsOption) {
        val analyzerComponent = DaggerAnalyzerComponent.factory()
            .create(
                inputProvider,
                outputProvider,
                projectInfoProvider,
                libName,
                logger
            )
        val analyzerMap = analyzerComponent.analyzerMap()
        val reportWriters = analyzerComponent.reportWriters()
        if (option == AnalyticsOption.DEFAULT) {
            analyzerMap
                .filterKeys { it != AnalyticsOption.LIB_CONTENT }
                .map { (key, analyzer) -> key to analyzer.process() }
                .onEach { (option, report) ->
                    reportWriters.forEach { reportWriter ->
                        reportWriter.write(
                            option.name.lowercase(Locale.getDefault()),
                            report
                        )
                    }
                }
        } else {
            analyzerMap[option]?.run {
                reportWriters.forEach { reportWriter ->
                    reportWriter.write(
                        option.name.lowercase(Locale.getDefault()),
                        process()
                    )
                }
            }
        }

    }
}