import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(TestLibraries.junit)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
}

tasks.withType<Test> {
    enabled = false
}

tasks.register<Jar>("testJar") {
    archiveFileName.set("junit4-integration-tests.jar")
    from(project.the<SourceSetContainer>()["test"].output)
}
