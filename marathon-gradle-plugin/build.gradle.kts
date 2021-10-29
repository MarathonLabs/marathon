plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jetbrains.dokka")
}


gradlePlugin {
    (plugins) {
        create("marathon-gradle-plugin") {
            id = "marathon"
            implementationClass = "com.malinskiy.marathon.gradle.MarathonPlugin"
        }
    }
}

Deployment.initialize(project)

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
    implementation(project(":configuration"))
    implementation(BuildPlugins.androidGradle)
    implementation(Libraries.apacheCommonsCodec)
}

tasks.processResources.configure {
    from(rootProject.project("cli").layout.buildDirectory.dir("distributions").get().asFile) {
        rename {
            if (it.endsWith(".zip") && it.contains("marathon")) {
                "marathon-cli.zip"
            } else {
                it
            }
        }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(rootProject.project("cli").tasks.getByName("distZip"))
}
