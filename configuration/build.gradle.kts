plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

dependencies {
    api(Libraries.jacksonDatabind)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.jacksonKotlin)
    implementation(Libraries.jacksonYaml)
    implementation(Libraries.jacksonJSR310)
    api(Libraries.apacheCommonsText)
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

buildConfig {
    buildConfigField("String", "VERSION", provider { "\"${Versions.marathon}\"" })
    buildConfigField("String", "RELEASE_MODE", provider {
        val releaseMode = Deployment.releaseMode ?: ""
        "\"$releaseMode\""
    })
}

setupTestTask()
setupDeployment()
setupKotlinCompiler()
