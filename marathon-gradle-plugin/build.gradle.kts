import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("com.gradle.plugin-publish") version Versions.gradlePluginPublish
    id("com.github.johnrengelman.shadow") version Versions.gradlePluginShadow
}


gradlePlugin {
    (plugins) {
        create("marathon-gradle-plugin") {
            id = "marathon"
            displayName = "Gradle plugin for Marathon test runner"
            description = "Marathon is a fast and platform-independent test runner focused on performance and stability"
            implementationClass = "com.malinskiy.marathon.gradle.MarathonPlugin"
        }
    }
}

pluginBundle {
    website = "https://marathonlabs.github.io/marathon/"
    vcsUrl = "https://github.com/MarathonLabs/marathon"
    tags = listOf("marathon", "test", "runner", "android")
}

Deployment.initialize(project)

dependencies {
    shadow(gradleApi())
    shadow(localGroovy())
    
    shadow(Libraries.kotlinLogging)
    implementation(project(":configuration"))
    shadow(BuildPlugins.androidGradle)
    shadow(Libraries.apacheCommonsCodec)
}

// needed to prevent inclusion of gradle-api into shadow JAR
afterEvaluate {
    configurations["api"].dependencies.remove(dependencies.gradleApi())
}

tasks.processResources.configure {
    val zipTask: Task = rootProject.project("cli").tasks.getByName("distZip")
    from(zipTask) {
        rename {
            "marathon-cli.zip"
        }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(zipTask)
}

tasks.shadowJar {
    isZip64 = true
    relocate(
        "com.fasterxml.jackson",
        "com.malinskiy.marathon.shadow.com.fasterxml.jackson"
    )
    archiveClassifier.set("")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.5"
}
