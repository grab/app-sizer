package com.grab.pax.plugins

import com.google.gson.Gson
import java.io.File
import java.io.FileWriter

class JsonFilePublisher(
    private val outPutDirectory: File,
    private val gson: Gson = Gson()
) : MetricsPublisher {
    override fun publish(reportId: String, metrics: List<Metrics>) {
        val outputFile = File(outPutDirectory, "$reportId-metrics.json")
        outputFile.initOutPutFile()
        FileWriter(outputFile).use {
            gson.toJson(metrics, it)
        }
    }

    private fun File.initOutPutFile() {
        if (!exists()) {
            if (!parentFile.exists())
                parentFile.mkdirs()
            createNewFile()
        }
    }
}