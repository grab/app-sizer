package com.grab.sizer.utils


const val DEFAULT_TAG = "AppSize"

interface Logger {
    fun log(tag: String, message: String)
    fun log(tag: String, e: Exception)
    fun logDebug(tag: String, message: String)
}

fun Logger.logDebug(message: String) {
    logDebug(DEFAULT_TAG, message)
}
fun Logger.log(message: String) {
    log(DEFAULT_TAG, message)
}

fun Logger.log(e: Exception) {
    log(DEFAULT_TAG, e)
}
