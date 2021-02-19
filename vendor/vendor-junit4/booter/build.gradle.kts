import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(TestLibraries.junit)
    implementation(Libraries.grpcNetty)
    implementation(project(":core"))
    implementation(project(":vendor:vendor-junit4:contract"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
}

val shadowJar = tasks.findByName("shadowJar")
task("copyToSampleProject", Copy::class) {
    from(shadowJar)
    val projectDir = rootProject.projectDir
    val sampleDir = File(projectDir, "sample")
    val junit4 = File(sampleDir, "gradle-junit4")
    into(junit4)

    dependsOn(shadowJar)
}
