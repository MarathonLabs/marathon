plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jetbrains.dokka")
}

gradlePlugin {
    (plugins) {
        create("marathon-junit4") {
            id = "marathon-junit4"
            implementationClass = "com.malinskiy.marathon.junit4.MarathonPlugin"
        }
    }
}

Deployment.initialize(project)

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
}
