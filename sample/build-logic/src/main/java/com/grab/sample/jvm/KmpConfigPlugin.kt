package com.grab.sample.jvm

import com.grab.sample.BuildConfig
import com.grab.sample.gradle.ConfigurablePlugin
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpConfigPlugin : ConfigurablePlugin({
    plugins.apply("org.jetbrains.kotlin.multiplatform")

    extensions.configure<KotlinMultiplatformExtension> {
        jvm()
    }

    kotlinCommon()
})