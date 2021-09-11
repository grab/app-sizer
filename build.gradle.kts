import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven(url = "https://repository.apache.org/content/repositories/snapshots/")
}

dependencies {
    implementation(files("libs/baksmali-2.5.2.jar"))
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.android.tools.build:bundletool:1.8.0")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.apache.poi:poi-ooxml:5.0.0")
    implementation("org.jxls:jxls-jexcel:1.0.6")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.21")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}