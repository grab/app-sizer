package com.grab.bundle.analytic

import com.grab.bundle.ApkFileInfo
import com.grab.bundle.Contributors

interface Analytic {
    fun analytic(apkInfo: ApkFileInfo, aarInfo: Map<String, ApkFileInfo>): Contributors
}



