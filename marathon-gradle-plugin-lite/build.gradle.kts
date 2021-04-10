plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jetbrains.dokka")
}


gradlePlugin {
    (plugins) {
        create("marathon-gradle-plugin-lite") {
            id = "marathon-lite"
            implementationClass = "com.malinskiy.marathon.lite.MarathonPlugin"
        }
    }
}

Deployment.initialize(project)

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
    implementation(project(":cli-configuration-schema"))
    implementation("org.yaml:snakeyaml:1.27")
//    implementation(BuildPlugins.androidGradle)
    implementation("com.android.tools.build:gradle:4.1.0") //use 4.1.0 instead of 4.0.0
}
