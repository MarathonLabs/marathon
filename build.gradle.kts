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
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC6-4"
}

configure<DetektExtension> {
    debug = true
    version = "1.0.0.RC6-4"
    profile = "main"

    profile("main", Action {
        input = rootProject.projectDir.absolutePath
        filters = ".*/resources/.*,.*/build/.*,.*/sample-app/.*"
        config = "${rootProject.projectDir}/default-detekt-config.yml"
        baseline = "${rootProject.projectDir}/reports/baseline.xml"
    })
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
