plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("marathon-junit4") version "0.7.0-SNAPSHOT"
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    testImplementation(TestLibraries.junit)
}
