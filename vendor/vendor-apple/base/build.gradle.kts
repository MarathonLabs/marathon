import com.malinskiy.marathon.buildsystem.XcresulttoolPlugin

plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

apply<XcresulttoolPlugin>()

dependencies {
    api(Libraries.kotlinStdLib)
    api(Libraries.kotlinCoroutines)
    api(Libraries.kotlinLogging)
    api(Libraries.kotlinReflect)
    api(Libraries.logbackClassic)
    api(Libraries.ddPlist)
    api(Libraries.guava)
    api(Libraries.rsync4j)

    api(Libraries.gson)
    api(Libraries.jacksonKotlin)
    api(Libraries.jacksonYaml)
    api(Libraries.jansi)
    api(Libraries.kotlinProcess)
    api(Libraries.sshj)

    api(project(":core"))

    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.assertk)
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

tasks.findByPath("sourcesJar")?.dependsOn(tasks.findByPath("generateXcresulttoolSource"))
