package com.grab.pax.plugins.report


interface MetricsPublisher{
    fun publish(reportId: String, metrics: List<Metrics>)
}

