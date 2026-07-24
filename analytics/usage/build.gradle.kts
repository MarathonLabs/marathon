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
    implementation(libs.okhttp)
    implementation(libs.kotlinStdLib)
    testRuntimeOnly(libs.jupiterEngine)
    testImplementation(libs.junit5)
    testImplementation(libs.kluent)
    testImplementation(libs.mockitoKotlin)
}
