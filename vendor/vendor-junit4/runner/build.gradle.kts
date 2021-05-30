import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

tasks.processResources.configure {
    from(rootProject.project("vendor:vendor-junit4:booter").layout.buildDirectory.dir("libs").get().asFile)
    dependsOn(rootProject.project("vendor:vendor-junit4:booter").tasks.getByName("shadowJar"))
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.logbackClassic)
    implementation(TestLibraries.junit)
    implementation(Libraries.asm)
    implementation(project(":core"))
    implementation(project(":vendor:vendor-junit4:contract"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
}
