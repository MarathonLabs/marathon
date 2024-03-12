plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":vendor:vendor-apple:base"))
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()

tasks.jar.configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
