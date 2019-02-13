import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.junit.platform.gradle.plugin")
}

Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.3"
}

dependencies {
    implementation(Analytics.googleAnalyticsWrapper)
    implementation(Libraries.kotlinStdLib)
    testCompile(TestLibraries.kluent)
    testCompile(TestLibraries.mockitoKotlin)
    testCompile(TestLibraries.spekAPI)
    testRuntime(TestLibraries.spekJUnitPlatformEngine)
}
