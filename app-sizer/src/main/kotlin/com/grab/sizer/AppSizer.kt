package com.grab.sizer

import com.grab.sizer.di.DaggerAnalyzerComponent
import com.grab.sizer.utils.InputProvider
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider

class AppSizer(
    private val inputProvider: InputProvider,
    private val outputProvider: OutputProvider,
    private val libName: String?,
    private val logger: Logger
) {
    fun process(option: AnalyticsOption) {
        val analyzerComponent = DaggerAnalyzerComponent.factory()
            .create(
                inputProvider = inputProvider,
                outputProvider = outputProvider,
                libName = libName,
                logger = logger
            )
        val analyzerMap = analyzerComponent.analyzerMap()
        val reportWriters = analyzerComponent.reportWriters()
        if (option == AnalyticsOption.DEFAULT) {
            analyzerMap
                .filterKeys { it != AnalyticsOption.LIB_CONTENT }
                .map { (key, analyzer) -> key to analyzer.process() }
                .onEach { (_, report) ->
                    reportWriters.forEach { reportWriter ->
                        reportWriter.write(
                            report
                        )
                    }
                }
        } else {
            analyzerMap[option]?.run {
                reportWriters.forEach { reportWriter ->
                    reportWriter.write(
                        process()
                    )
                }
            }
        }

    }
}