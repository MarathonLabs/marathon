plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    `signing`
}

gradlePlugin {
    (plugins) {
        "marathon-gradle-plugin" {
            id = "marathon"
            implementationClass = "com.malinskiy.marathon.MarathonPlugin"
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    from(java.docsDir)
    dependsOn("javadoc")
}

publishing {
    publications {
        create("default", MavenPublication::class.java) {
            Deployment.customizePom(project, pom)
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
    repositories {
        maven {
            name = "Local"
            setUrl("$rootDir/build/repository")
        }
        maven {
            name = "OSSHR"
            credentials {
                username = Deployment.user
                password = Deployment.password
            }
            setUrl(Deployment.deployUrl)
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
    implementation(project(":core"))
    implementation(project(":vendor-android"))
    implementation(BuildPlugins.androidGradle)
}
