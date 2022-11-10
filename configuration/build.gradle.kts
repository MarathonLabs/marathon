import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.junit.platform.gradle.plugin")
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

tasks.named<JacocoReport>("jacocoTestReport").configure {
    reports.xml.required.set(true)
    reports.html.required.set(true)
    dependsOn(tasks.named("test"))
}

tasks.withType<Test>().all {
    tasks.getByName("check").dependsOn(this)
    useJUnitPlatform()
}

junitPlatform {
    enableStandardTestTask = true
}

tasks.getByName("junitPlatformTest").outputs.upToDateWhen { false }
tasks.getByName("test").outputs.upToDateWhen { false }

Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.5"
}
