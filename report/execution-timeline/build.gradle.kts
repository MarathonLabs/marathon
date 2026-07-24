plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlinStdLib)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinLogging)
}

setupDeployment()
setupKotlinCompiler()
