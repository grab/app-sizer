package com.grab.bundle.analyzer

import com.grab.bundle.FileInfo
import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo

typealias RawContributors = Map<String, Set<FileInfo>>

interface Analyzer {
    fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars : Set<JarFileInfo>): RawContributors
}



