pluginManagement {
    repositories {
        maven { url = uri("$rootDir/build/repository") }
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "marathon"
include("core")
include("configuration")
include("vendor:vendor-android")
include("vendor:vendor-apple:ios")
include("vendor:vendor-apple:macos")
include("vendor:vendor-apple:base")
include("vendor:vendor-test")
include("report:html-report")
include("report:execution-timeline")
include("cli")
include(":analytics:usage")
