plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // Keep in lockstep with `Versions.kotlin` in src/main/kotlin/Versions.kt.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.google.code.gson:gson:2.11.0")
}
