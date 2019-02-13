pluginManagement {
    repositories {
        google()
        maven { url = uri("$rootDir/../../build/repository") }
        gradlePluginPortal()
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

rootProject.name = "android-app"
rootProject.buildFileName = "build.gradle.kts"
include("app")
