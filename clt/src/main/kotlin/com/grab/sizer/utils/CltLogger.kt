package com.grab.sizer.utils

class CltLogger : Logger {
    override fun log(tag: String, message: String) {
        println("$tag : $message")
    }

    override fun log(tag: String, e: Exception) {
        println("$tag :")
        e.printStackTrace()
    }
}