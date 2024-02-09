plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    api(Libraries.sshj)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()
