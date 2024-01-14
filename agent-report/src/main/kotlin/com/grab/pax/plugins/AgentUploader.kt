package com.grab.pax.plugins

import java.io.File
import kotlin.system.measureTimeMillis


class DirAgentUploader(private val jsonMetricsDir: File,
                       private val agent: String) : AgentUploader {
    override fun upload() {
        val timeTaken = measureTimeMillis {
            if (!jsonMetricsDir.isDirectory || jsonMetricsDir.listFiles().isEmpty()) {
                throw IllegalArgumentException("The ${jsonMetricsDir.absolutePath} is not a directory or empty")
            }

            jsonMetricsDir.listFiles().map { FileAgentUploader(it.absolutePath, agent) }
                    .forEach { it.upload() }
        }

        println("Agent Uploaded successfully, it take ${timeTaken / 1000} seconds")
    }
}

class FileAgentUploader(private val filePath: String,
                        private val agent: String) : AgentUploader {
    override fun upload() {
        Runtime.getRuntime().exec("$agent -f $filePath").waitFor()
    }
}

interface AgentUploader {
    fun upload()
}