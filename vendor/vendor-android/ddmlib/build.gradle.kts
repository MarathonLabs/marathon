import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.junit.platform.gradle.plugin")
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.allure)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    api(Libraries.ddmlib)
    implementation(Libraries.dexTestParser)
    implementation(Libraries.axmlParser)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.scalr)
    implementation(project(":core"))
    implementation(project(":vendor:vendor-android:base"))
    implementation(Libraries.logbackClassic)
    testImplementation(project(":vendor:vendor-test"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.koin)
    testImplementation(TestLibraries.junit5)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
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
