
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    `java-library`
    id("com.google.protobuf") version "0.8.8"
    id("de.fuerstenau.buildconfig") version "1.1.8"
}

dependencies {
    api("com.google.protobuf:protobuf-java:3.15.8")
}

Deployment.initialize(project)
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.15.8"
    }
}
buildConfig {
    appName = project.name
    version = Versions.marathon
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}
