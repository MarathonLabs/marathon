pluginManagement {
    repositories {
        maven { url = uri("$rootDir/build/repository") }
        gradlePluginPortal()
        google()
    }
}

// Auto-provisions Java toolchains from Adoptium / etc. when a matching JDK is
// not already installed. Required for `java.toolchain.languageVersion = 11`
// (declared in buildSrc) on hosts where JDK 11 is not on disk.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
