plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":vendor:vendor-apple:base"))
    testImplementation(libs.kluent)
    testImplementation(libs.assertk)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.testContainers)
    testImplementation(libs.testContainersJupiter)
    testImplementation(libs.junit5)
    testImplementation(libs.coroutinesTest)
    testRuntimeOnly(libs.jupiterEngine)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()

tasks.jar.configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
