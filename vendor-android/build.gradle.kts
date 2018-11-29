import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.junit.platform.gradle.plugin")
}

kotlin.experimental.coroutines = Coroutines.ENABLE

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.ddmlib)
    implementation(Libraries.dexTestParser)
    implementation(Libraries.axmlParser)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.scalr)
    implementation(project(":core"))
    testImplementation(project(":vendor-test"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.spekAPI)
    testRuntime(TestLibraries.spekJUnitPlatformEngine)
}

Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.2"
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
