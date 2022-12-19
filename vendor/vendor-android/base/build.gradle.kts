plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.allure)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.dexTestParser)
    implementation(Libraries.axmlParser)
    implementation(Libraries.jacksonAnnotations)
    implementation(project(":core"))
    implementation(Libraries.logbackClassic)
    implementation(Libraries.androidCommon)
    testImplementation(project(":vendor:vendor-test"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.junit5)
    testRuntimeOnly(TestLibraries.jupiterEngine)
    testImplementation(TestLibraries.koin)
    testImplementation(TestLibraries.adamServerStubJunit5)
    testImplementation(project(":vendor:vendor-android:adam"))
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()
