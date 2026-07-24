plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(libs.kotlinStdLib)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinLogging)
    implementation(libs.kotlinReflect)
    implementation(libs.gson)
    implementation(libs.jsonAssert)
    implementation(libs.xmlUnit)
    implementation(libs.kluent)
    implementation(libs.mockitoKotlin)
    implementation(project(":core"))
    implementation(project(":analytics:usage"))
}

setupKotlinCompiler()
