import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.gradle.api.plugins.ExtensionAware
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    `application`
    id("idea")
    id("org.jetbrains.kotlin.jvm")
    id("org.junit.platform.gradle.plugin")
    id("de.fuerstenau.buildconfig") version "1.1.8"
}

application {
    mainClassName = "com.malinskiy.marathon.cli.ApplicationViewKt"
    applicationName = "marathon"
    applicationDefaultJvmArgs = listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044",
        "-Dkotlinx.coroutines.experimental.debug=onR")
}

distributions {
    getByName("main") {
        baseName = "marathon"
    }
}

kotlin.experimental.coroutines = Coroutines.ENABLE

dependencies {
    implementation(project(":core"))
    implementation(project(":vendor-ios"))
    implementation(project(":vendor-android"))
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.slf4jAPI)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.argParser)
    implementation(Libraries.jacksonDatabind)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.jacksonKotlin)
    implementation(Libraries.jacksonYaml)
    implementation(Libraries.jacksonJSR310)
    testCompile(TestLibraries.kluent)
    testCompile(TestLibraries.spekAPI)
    testRuntime(TestLibraries.spekJUnitPlatformEngine)
}

Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.2"
}

buildConfig {
    appName = project.name
    version = Versions.marathon
}

sourceSets["main"].java {
    srcDirs.add(File(buildDir, "gen"))
}

// At the moment for non-Android projects you need to explicitly
// mark the generated code for correct highlighting in IDE.
idea {
    module {
        sourceDirs = sourceDirs + file("${project.buildDir}/gen/buildconfig/src/main")
        generatedSourceDirs = generatedSourceDirs + file("${project.buildDir}/gen/buildconfig/src/main")
    }
}

junitPlatform {
    filters {
        engines {
            include("spek")
        }
    }
}

// extension for configuration
fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw IllegalArgumentException("${this::class} must be an instance of ExtensionAware")
    }
}

fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw IllegalArgumentException("${this::class} must be an instance of ExtensionAware")
    }
}
