pluginManagement {
    repositories {
        google()
        maven { url = uri("$rootDir/../../build/repository") }
        gradlePluginPortal()
        mavenLocal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "marathon-junit4") {
                useModule("com.malinskiy.marathon:gradle-junit4:${requested.version}")
            }
        }
    }
}

rootProject.name = "gradle-junit4"
rootProject.buildFileName = "build.gradle.kts"
include("library")
