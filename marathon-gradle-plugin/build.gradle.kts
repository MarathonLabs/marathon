import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
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
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            groupId = "com.malinskiy.marathon"
            artifactId = "marathon-gradle-plugin"
            version = Versions.marathon
        }
    }
    repositories {
        maven(url = "$rootDir/build/repository")
    }
}

bintray {
    user = Bintray.user
    key = Bintray.key
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "marathon"
        name = "marathon-gradle-plugin"
        vcsUrl = "https://github.com/Malinskiy/marathon"
        setLicenses("Apache-2.0")
    })
}

dependencies {
    implementation(gradleApi())
    implementation(Libraries.kotlinLogging)
    implementation(project(":core"))
    implementation(project(":vendor-android"))
    implementation(BuildPlugins.androidGradle)
}
