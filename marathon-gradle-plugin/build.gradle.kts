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
    group = "com.malinskiy"
    version  = "0.1.0"
    repositories {
        maven(url = "$rootDir/build/repository")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
    implementation(project(":core"))
    implementation(project(":vendor-android"))
    implementation(BuildPlugins.androidGradle)
}
