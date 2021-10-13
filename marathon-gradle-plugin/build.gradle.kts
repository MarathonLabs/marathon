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
    implementation(project(":core"))
    implementation(project(":vendor:vendor-android:base"))
    implementation(project(":vendor:vendor-android:adam"))
    implementation(project(":vendor:vendor-android:ddmlib"))
    implementation(BuildPlugins.androidGradle)
    implementation(project(":analytics:usage"))
}
