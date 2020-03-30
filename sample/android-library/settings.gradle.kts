pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenLocal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            } else
                if (requested.id.id == "marathon") {
                    useModule("com.malinskiy.marathon:marathon-gradle-plugin:${requested.version}")
                }
        }
    }
}

rootProject.name = "android-library"
rootProject.buildFileName = "build.gradle.kts"
include("library")
