import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
        classpath(BuildPlugins.dokka)
    }
}


plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("com.github.ben-manes.versions") version "0.51.0"
}

configure<DetektExtension> {
    debug = true
    source.from(rootProject.projectDir)
    config.from(rootProject.projectDir.resolve("default-detekt-config.yml"))
    baseline = file("${rootProject.projectDir}/reports/baseline.xml")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

tasks.withType<Detekt> {
    exclude(".*/resources/.*")
    exclude(".*/build/.*")
    exclude(".*/sample-app/.*")
}

allprojects {
    group = "com.malinskiy.marathon"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }

    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin"
                    && (requested.name.startsWith("kotlin-stdlib") || requested.name.startsWith("kotlin-reflect"))
                ) {
                    useVersion(Versions.kotlin)
                }
            }
        }
    }
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "latest"
    retries = 3
    retryBackOffMs = 500
}

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.ADOPTIUM
}
