package com.grab.pax.plugins


interface MetricsPublisher{
    fun publish(reportId: String, metrics: List<Metrics>)
}

