plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    implementation(project(":vendor:vendor-junit4:vendor-junit4-runner-contract"))
    compileOnly(TestLibraries.junit)
}
