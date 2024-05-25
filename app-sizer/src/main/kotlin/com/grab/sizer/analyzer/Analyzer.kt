package com.grab.sizer.analyzer


/**
 * An Analyzer interface represents the logic that needs to be executed based on the [com.grab.sizer.AnalyticsOption]
 * Each [com.grab.sizer.AnalyticsOption] should have an implement of Analyzer
 * It produces its own metric/graph/dashboard and should be designed to operate independently of other analyzers.
 */
interface Analyzer {
    fun process()
}