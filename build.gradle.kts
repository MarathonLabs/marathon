import io.gitlab.arturbosch.detekt.extensions.DetektExtension

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
        classpath(BuildPlugins.junitGradle)
        classpath(BuildPlugins.dokka)
    }
}


plugins {
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    id("com.github.ben-manes.versions") version "0.39.0"
}

configure<DetektExtension> {
    debug = true
    input = files(
        rootProject.projectDir.absolutePath
    )
    config = files("${rootProject.projectDir}/default-detekt-config.yml")
    baseline = file("${rootProject.projectDir}/reports/baseline.xml")
}

tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class).configureEach {
    exclude(".*/resources/.*")
    exclude(".*/build/.*")
    exclude(".*/sample-app/.*")
}

allprojects {
    group = "com.malinskiy.marathon"

    repositories {
        mavenLocal()
        jcenter()
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
