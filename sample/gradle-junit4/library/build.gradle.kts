import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    testImplementation(TestLibraries.junit)
}

task("testJar", ShadowJar::class) {
    classifier = "tests"
    from(sourceSets.test.get().output)
    configurations = listOf(project.configurations.testRuntime.get())
}

task("prepareMarathonBundle") {
    dependsOn("testJar", "shadowJar")
}
