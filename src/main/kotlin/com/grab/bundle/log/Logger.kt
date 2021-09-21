package com.grab.bundle.log


inline fun log(value : String){
    println(value)
}

inline fun log(e : Exception){
    e.printStackTrace()
}