plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":vendor:vendor-apple:base"))
    implementation(Libraries.ktorNetwork)

    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.assertk)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.coroutinesTest)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()

tasks.jar.configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
