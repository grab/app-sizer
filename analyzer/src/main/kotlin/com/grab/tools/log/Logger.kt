package com.grab.tools.log


inline fun log(value : String){
    println(value)
}

inline fun log(e : Exception){
    e.printStackTrace()
}