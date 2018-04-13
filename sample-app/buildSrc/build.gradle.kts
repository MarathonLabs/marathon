import org.gradle.kotlin.dsl.`kotlin-dsl`

buildscript {
    val kotlinVersion = file("../kotlin-version").readText().trim()

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    `kotlin-dsl`
}