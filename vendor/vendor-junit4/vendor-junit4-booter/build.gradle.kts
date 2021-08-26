import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

tasks.processResources.configure {
    from(rootProject.project("vendor:vendor-junit4:vendor-junit4-runner").layout.buildDirectory.dir("libs").get().asFile)
    dependsOn(rootProject.project("vendor:vendor-junit4:vendor-junit4-runner").tasks.getByName("shadowJar"))
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(TestLibraries.junit)
    implementation(TestLibraries.junit5launcher)
    implementation(TestLibraries.junit5vintage)
    implementation(Libraries.grpcNetty)
    implementation(project(":core"))
    implementation(project(":vendor:vendor-junit4:vendor-junit4-booter-contract"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
}
