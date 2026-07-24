import com.malinskiy.marathon.buildsystem.XcresulttoolPlugin

plugins {
    `java-library`
    jacoco
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

apply<XcresulttoolPlugin>()

dependencies {
    api(libs.kotlinStdLib)
    api(libs.kotlinCoroutines)
    api(libs.kotlinLogging)
    api(libs.kotlinReflect)
    api(libs.logbackClassic)
    api(libs.ddPlist)
    api(libs.guava)
    api(libs.rsync4j)

    api(libs.gson)
    api(libs.jacksonKotlin)
    api(libs.jacksonYaml)
    api(libs.jansi)
    api(libs.kotlinProcess)
    api(libs.sshj)

    api(project(":core"))

    testImplementation(libs.kluent)
    testImplementation(libs.assertk)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.testContainers)
    testImplementation(libs.testContainersJupiter)
    testImplementation(libs.junit5)
    testImplementation(libs.coroutinesTest)
    testRuntimeOnly(libs.jupiterEngine)
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()

tasks.findByPath("generateXcresulttoolSource")?.let { generator ->
    tasks.findByPath("sourcesJar")?.dependsOn(generator)
}
