plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
}

dependencies {
    implementation(libs.kotlinStdLib)
    implementation(libs.allure)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinLogging)
    implementation(libs.dexTestParser)
    implementation(libs.axmlParser)
    implementation(libs.jacksonAnnotations)
    implementation(project(":core"))
    implementation(libs.logbackClassic)
    implementation(libs.androidCommon)
    implementation(libs.adam)
    implementation(libs.adamTestrunnerContract)
    testImplementation(project(":vendor:vendor-test"))
    testImplementation(libs.kluent)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.junit5)
    testRuntimeOnly(libs.jupiterEngine)
    testImplementation(libs.koinTest)
    testImplementation(libs.adamServerStubJunit5)
    testImplementation(libs.coroutinesTest)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()
