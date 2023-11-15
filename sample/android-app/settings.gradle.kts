pluginManagement {
    repositories {
        google()
        maven { url = uri("$rootDir/../../build/repository") }
        gradlePluginPortal()
        mavenLocal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            } else
                if (requested.id.id == "com.malinskiy.marathon") {
                    useModule("com.malinskiy.marathon:marathon-gradle-plugin:${requested.version}")
                }
        }
    }
}

rootProject.name = "android-app"
rootProject.buildFileName = "build.gradle.kts"
include("app")
include("ui-tests")
