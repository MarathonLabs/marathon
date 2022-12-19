import com.malinskiy.marathon.buildsystem.XcresulttoolPlugin

plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

apply<XcresulttoolPlugin>()

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.kotlinReflect)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.ddPlist)
    implementation(Libraries.guava)
    implementation(Libraries.rsync4j)
    implementation(Libraries.sshj)
    implementation(Libraries.gson)
    implementation(Libraries.jacksonKotlin)
    implementation(Libraries.jacksonYaml)
    implementation(Libraries.jansi)
    implementation(Libraries.kotlinProcess)
    implementation(project(":core"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.testContainers)
    testImplementation(TestLibraries.testContainersJupiter)
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.coroutinesTest)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()
