plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

dependencies {
    api(Libraries.jacksonDatabind)
    api(Libraries.jacksonAnnotations)
    api(Libraries.jacksonKotlin)
    api(Libraries.jacksonYaml)
    api(Libraries.jacksonJSR310)
    api(Libraries.apacheCommonsText)
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

buildConfig {
    useKotlinOutput { internalVisibility = false }

    buildConfigField("String", "VERSION", provider { "\"${Versions.marathon}\"" })
    buildConfigField("String", "RELEASE_MODE", provider {
        val releaseMode = Deployment.releaseMode ?: ""
        "\"$releaseMode\""
    })
}

setupTestTask()
setupDeployment()
setupKotlinCompiler()
