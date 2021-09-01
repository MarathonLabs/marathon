plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    api(project(":vendor:vendor-junit4:vendor-junit4-runner-contract"))
    compileOnly(TestLibraries.junit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
