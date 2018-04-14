plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

gradlePlugin {
    (plugins) {
        "marathon-gradle-plugin" {
            id = "marathon"
            implementationClass = "com.malinskiy.marathon.MarathonPlugin"
        }
    }
}

publishing {
    repositories {
        maven(url = "$rootDir/build/repository")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.slf4jSimple)
    implementation(project(":core"))
    implementation(project(":vendor-android"))
    implementation(BuildPlugins.androidGradle)
}