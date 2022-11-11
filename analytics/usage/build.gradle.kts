plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

setupDeployment()
setupKotlinCompiler()

dependencies {
    implementation(Analytics.googleAnalyticsWrapper)
    implementation(Libraries.kotlinStdLib)
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
}
