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
include("marathon-gradle-plugin")
include("report:html-report")
include("report:execution-timeline")
include("cli")
include(":analytics:usage")
