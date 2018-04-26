buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
        classpath(BuildPlugins.junitGradle)
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC6-4"
}

allprojects {
    group = "com.malinskiy"
    version = "0.1.0"

    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}