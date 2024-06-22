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
            username = "svc.eng.gfinmobile"
            password = "cmVmdGtuOjAxOjE3NDU2NTU0MTE6aUpQVjFseTRLd1RDNTU4TnZMYjZCQzJaVGFY"
        }
    }
}

dependencies {
    implementation (libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("sizerKotlinBuildPlugin"){
            id = "com.grab.sizer.build"
            implementationClass = "com.grab.sizer.buildplugins.AppSizerConfigPlugin"
        }
    }
}