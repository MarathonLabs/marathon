plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()

dependencies {
    implementation(Libraries.okhttp)
    implementation(Libraries.kotlinStdLib)
    testRuntimeOnly(TestLibraries.jupiterEngine)
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
}
