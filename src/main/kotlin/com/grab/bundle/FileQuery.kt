package com.grab.bundle

import java.io.File
import java.io.IOException

class FileQuery {
    @Throws(IOException::class)
    fun searchFile(dir: File, vararg extensions: String): Sequence<File> {
        if (dir.isFile) throw IOException("${dir.path} is not a directory")
        return dir.walk()
            .filter { it.isFile && extensions.any { ext -> it.extension.equals(ext, true) } }
            .asSequence()
    }
}

