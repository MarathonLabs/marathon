import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    java
    id("org.jetbrains.kotlin.jvm")
    id("org.junit.platform.gradle.plugin")
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
}

kotlin.experimental.coroutines = Coroutines.ENABLE

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.ddmlib)
    implementation(Libraries.dexTestParser)
    implementation(Libraries.axmlParser)
    implementation(project(":core"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.spekAPI)
    testRuntime(TestLibraries.spekJUnitPlatformEngine)
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    from(java.docsDir)
    dependsOn("javadoc")
}

publishing {
    publications {
        create("default", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            groupId = "com.malinskiy.marathon"
            artifactId = "vendor-android"
            version = Versions.marathon
        }
    }
    repositories {
        maven(url = "$rootDir/build/repository")
    }
}

bintray {
    user = Bintray.user
    key = Bintray.key
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "marathon"
        name = "vendor-android"
        vcsUrl = "https://github.com/Malinskiy/marathon"
        setLicenses("Apache-2.0")
    })
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
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
