plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    api(libs.jacksonDatabind)
    api(libs.jacksonAnnotations)
    api(libs.jacksonKotlin)
    api(libs.jacksonYaml)
    api(libs.jacksonJSR310)
    api(libs.apacheCommonsText)
    testImplementation(libs.junit5)
    testImplementation(libs.kluent)
    testImplementation(libs.mockitoKotlin)
    testRuntimeOnly(libs.jupiterEngine)
}

buildConfig {
    useKotlinOutput { internalVisibility = false }

    buildConfigField("String", "VERSION", provider { "\"${Deployment.getVersion(project)}\"" })
    buildConfigField("String", "RELEASE_MODE", provider {
        val releaseMode = Deployment.releaseMode ?: ""
        "\"$releaseMode\""
    })
}

setupTestTask()
setupDeployment()
setupKotlinCompiler()
