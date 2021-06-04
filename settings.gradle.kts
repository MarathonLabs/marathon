pluginManagement {
    repositories {
        maven { url = uri("$rootDir/build/repository") }
        gradlePluginPortal()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

rootProject.name = "marathon"
include("core")
include("vendor:vendor-android:base")
include("vendor:vendor-android:ddmlib")
include("vendor:vendor-android:adam")
include("vendor:vendor-ios")
include("vendor:vendor-test")
include("vendor:vendor-junit4:booter")
include("vendor:vendor-junit4:runner")
include("vendor:vendor-junit4:contract")
include("marathon-gradle-plugin")
include("gradle-plugin:gradle-junit4")
include("report:html-report")
include("report:execution-timeline")
include("cli")
include(":analytics:usage")
