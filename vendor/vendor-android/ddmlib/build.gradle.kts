plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.allure)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.ddmlib)
    implementation(Libraries.androidCommon)
    implementation(Libraries.dexTestParser)
    implementation(Libraries.axmlParser)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.scalr)
    implementation(project(":core"))
    implementation(project(":vendor:vendor-android:base"))
    implementation(Libraries.logbackClassic)
    testImplementation(project(":vendor:vendor-test"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.koin)
    testImplementation(TestLibraries.junit5)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()
