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
    implementation("com.android.tools.build:gradle:4.1.0") //use 4.1.0 instead of 4.0.0
    implementation(Libraries.jacksonDatabind)
    implementation(Libraries.jacksonAnnotations)
    implementation(Libraries.jacksonKotlin)
    implementation(Libraries.jacksonYaml)
    implementation(Libraries.jacksonJSR310)
}
