/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

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