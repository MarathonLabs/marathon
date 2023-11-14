plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.google.code.gson:gson:2.10.1")
}
