package com.grab.tools.utils

import java.io.File
import java.io.IOException


interface FileQuery{
    fun query(dir: File, vararg extensions: String): Sequence<File>
}

class DefaultFileQuery : FileQuery {

    @Throws(IOException::class)
    override fun query(dir: File, vararg extensions: String): Sequence<File> {
        if (dir.isFile) throw IOException("${dir.path} is not a directory")
        return dir.walk()
            .filter { it.isFile && extensions.any { ext -> it.extension.equals(ext, true) } }
            .asSequence()
    }
}



