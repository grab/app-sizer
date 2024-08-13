plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}


repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven {
        setUrl("https://artifacts.gitlab.myteksi.net/artifactory/mobile--android")
        credentials {
            username = System.getenv("READ_USER")
            password = System.getenv("READ_PASSWORD")
        }
    }
}

dependencies {
    implementation (libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("sizerKotlinBuildPlugin"){
            id = "com.grab.sizer.kotlin"
            implementationClass = "com.grab.sizer.buildplugin.AppSizerConfigPlugin"
        }
    }
}