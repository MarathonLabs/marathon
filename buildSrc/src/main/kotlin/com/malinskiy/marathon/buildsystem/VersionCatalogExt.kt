package com.malinskiy.marathon.buildsystem

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

// Precompiled Kotlin sources in buildSrc/ don't have Gradle's generated
// typed `libs` accessor. These helpers give them the same lookup ergonomics
// as regular build.gradle.kts files without the verbose
// `extensions.getByType<...>().named("libs").findX("...").get()` chain.
//
// Namespaced so `import com.malinskiy.marathon.buildsystem.libs` opts in
// deliberately; build.gradle.kts files continue to use Gradle's generated
// typed accessor without collision.
//
// Usage:
//   project.libs.library("junitPlatformLauncher")
//   project.libs.version("kotlin")
//   project.libs.plugin("dokka")

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.version(alias: String): VersionConstraint =
    findVersion(alias).orElseThrow {
        NoSuchElementException("Version '$alias' not declared in libs.versions.toml")
    }

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow {
        NoSuchElementException("Library '$alias' not declared in libs.versions.toml")
    }

fun VersionCatalog.bundle(alias: String): Provider<ExternalModuleDependencyBundle> =
    findBundle(alias).orElseThrow {
        NoSuchElementException("Bundle '$alias' not declared in libs.versions.toml")
    }

fun VersionCatalog.plugin(alias: String): Provider<PluginDependency> =
    findPlugin(alias).orElseThrow {
        NoSuchElementException("Plugin '$alias' not declared in libs.versions.toml")
    }
