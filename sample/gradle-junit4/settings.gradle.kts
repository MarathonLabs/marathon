pluginManagement {
    repositories {
        google()
        maven { url = uri("$rootDir/../../build/repository") }
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "gradle-junit4"
rootProject.buildFileName = "build.gradle.kts"
include("library")
