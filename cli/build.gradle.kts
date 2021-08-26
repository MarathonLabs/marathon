import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("idea")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.junit.platform.gradle.plugin")
    id("de.fuerstenau.buildconfig") version "1.1.8"
}

val enableJDB = false
val debugCoroutines = true
val jvmOptions = listOf(
    when (enableJDB) {
        true -> "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044"
        else -> ""
    },
    when (debugCoroutines) {
        true -> "-Dkotlinx.coroutines.debug=on"
        else -> ""
    }
).filter { it.isNotBlank() }

application {
    mainClass.set("com.malinskiy.marathon.cli.ApplicationViewKt")
    applicationName = "marathon"
    applicationDefaultJvmArgs = jvmOptions
}

distributions {
    getByName("main") {
        distributionBaseName.set("marathon")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":vendor:vendor-ios"))
    implementation(project(":vendor:vendor-android:base"))
    implementation(project(":vendor:vendor-android:ddmlib"))
    implementation(project(":vendor:vendor-android:adam"))
    implementation(project(":vendor:vendor-junit4:vendor-junit4-core"))
    implementation(project(":analytics:usage"))
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.kotlinReflect)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.argParser)
    implementation(Libraries.jacksonDatabind)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.jacksonKotlin)
    implementation(Libraries.jacksonYaml)
    implementation(Libraries.jacksonJSR310)
    implementation(Libraries.apacheCommonsText)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.junit5)
    testRuntimeOnly(TestLibraries.jupiterEngine)
}

Deployment.initialize(project)

buildConfig {
    appName = project.name
    version = Versions.marathon
}

sourceSets["main"].java {
    srcDirs.add(File(buildDir, "gen"))
}

// At the moment for non-Android projects you need to explicitly
// mark the generated code for correct highlighting in IDE.
idea {
    module {
        sourceDirs = sourceDirs + file("${project.buildDir}/gen/buildconfig/src/main")
        generatedSourceDirs = generatedSourceDirs + file("${project.buildDir}/gen/buildconfig/src/main")
    }
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
