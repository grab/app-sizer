package com.grab.pax.plugins.report

import com.google.gson.Gson
import java.io.File
import java.io.FileWriter

class JsonFilePublisher(private val file: File,
                        private val gson: Gson = Gson()) : MetricsPublisher {
    override fun publish(metrics: List<Metrics>) {
        initOutPutFile()
        FileWriter(file).run {
            gson.toJson(metrics, this)
            this.close()
        }
    }

    private fun initOutPutFile() {
        if (!file.exists()) {
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()
            file.createNewFile()
        }
    }
}