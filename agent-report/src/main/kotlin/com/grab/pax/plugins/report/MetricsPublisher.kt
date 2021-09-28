package com.grab.pax.plugins.report


interface MetricsPublisher{
    fun publish(metrics: List<Metrics>)
}

