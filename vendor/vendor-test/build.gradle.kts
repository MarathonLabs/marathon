plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.kotlinReflect)
    implementation(Libraries.gson)
    implementation(TestLibraries.jsonAssert)
    implementation(TestLibraries.xmlUnit)
    implementation(TestLibraries.kluent)
    implementation(TestLibraries.mockitoKotlin)
    implementation(project(":core"))
}

setupKotlinCompiler()
