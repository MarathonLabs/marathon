import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    // Kotlin plugin is on the classpath via `buildSrc` (which pulls it in
    // from the version catalog). Declaring an explicit version here would
    // trigger Gradle's "plugin already on classpath, unknown version" error.
    id("org.jetbrains.kotlin.jvm") apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.node) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.benManesVersions)
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

// Pin all Kotlin stdlib/reflect fetches to the version the catalog declares.
// Hoisted out of `allprojects` so the accessor is evaluated once at
// root-config time, not re-resolved in each subproject.
val kotlinVersion = libs.versions.kotlin.get()

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
                    useVersion(kotlinVersion)
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

