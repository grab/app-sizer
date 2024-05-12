package com.grab.sizer.utils

import java.io.File
import java.io.IOException
import javax.inject.Inject


interface FileQuery {
    fun query(dir: File, vararg extensions: String): Sequence<File>
}

class DefaultFileQuery @Inject constructor() : FileQuery {

    @Throws(IOException::class)
    override fun query(dir: File, vararg extensions: String): Sequence<File> {
        if (dir.isFile) throw IOException("${dir.path} is not a directory")
        return dir.walk()
            .filter { file ->
                file.isFile && extensions.any { ext ->
                    file.extension.equals(ext, true)
                }
            }
    }
}



