import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    idea
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets["main"].output
        compileClasspath += sourceSets["test"].output
        compileClasspath += configurations.testCompileClasspath

        runtimeClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["test"].output
        runtimeClasspath += configurations.testRuntimeClasspath
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDirs("src/integrationTest/kotlin")
        }
    }
}

buildConfig {
    buildConfigField("String", "NAME", "\"${project.name}\"")
    buildConfigField("String", "VERSION", provider<String> { "\"${Versions.marathon}\"" })
    buildConfigField("String", "BUGSNAG_TOKEN", provider {
        val token = System.getenv("BUGSNAG_TOKEN") ?: ""
        "\"$token\""
    })
    buildConfigField("String", "RELEASE_MODE", provider {
        val releaseMode = Deployment.releaseMode ?: ""
        "\"$releaseMode\""
    })
}

dependencies {
    api(project(":configuration"))
    implementation(project(":report:html-report"))
    implementation(project(":report:execution-timeline"))

    implementation(Libraries.allure)
    implementation(Libraries.allureKotlinCommons)
    implementation(Libraries.allureEnvironment)
    implementation(Libraries.allureTestFilter)

    implementation(project(":analytics:usage"))
    implementation(Libraries.gson)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.apacheCommonsIO)
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.influxDbClient)
    implementation(Libraries.influxDb2Client)
    implementation(Libraries.scalr)
    api(Libraries.koin)
    api(Libraries.bugsnag)
    testImplementation(project(":vendor:vendor-test"))
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.testContainers)
    testImplementation(TestLibraries.testContainersInflux)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.koin)
    testImplementation(TestLibraries.xmlUnit)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    exclude("**/resources/**")

    shouldRunAfter("test")
}

setupDeployment()
setupKotlinCompiler()
setupTestTask()
