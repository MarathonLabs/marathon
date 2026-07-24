plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin version drawn from the root version catalog (buildSrc/settings.gradle.kts
    // wires `../gradle/libs.versions.toml` in as `libs`).
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation(libs.gson)
}
