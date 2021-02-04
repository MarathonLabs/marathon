plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jetbrains.dokka")
}


gradlePlugin {
    (plugins) {
        create("marathon-gradle-plugin-lite") {
            id = "marathon"
            implementationClass = "com.malinskiy.marathon.MarathonPluginLite"
        }
    }
}

Deployment.initialize(project)

dependencies {
    implementation(gradleApi())
    implementation(BuildPlugins.androidGradle)
    implementation("org.yaml:snakeyaml:1.25")
    implementation(kotlin("stdlib"))
}
