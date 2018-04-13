import java.io.File

object Versions {
    val kotlin = File("kotlin-version").readText().trim()

    val ddmlib = "26.1.1"
    val kotlinLogging = "1.4.9"

    val junitGradle = "1.0.0"
    val androidGradleVersion = "3.0.1"

    val spek = "1.1.5"
    val kluent = "1.35"

    val kakao = "1.2.1"
    val espresso = "3.0.1"
    val espressoRules = "1.0.1"
    val espressoRunner = "1.0.1"
    val junit = "4.12"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val junitGradle = "org.junit.platform:junit-platform-gradle-plugin:${Versions.junitGradle}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
}

object Libraries {
    val ddmlib = "com.android.tools.ddms:ddmlib:${Versions.ddmlib}"
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    val kotlinLogging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"
}

object TestLibraries {
    val spekAPI = "org.jetbrains.spek:spek-api:${Versions.spek}"
    val spekJUnitPlatformEngine = "org.jetbrains.spek:spek-junit-platform-engine:${Versions.spek}"
    val kluent = "org.amshove.kluent:kluent:${Versions.kluent}"
    val kakao = "com.agoda.kakao:kakao:${Versions.kakao}"

    val espressoRunner = "com.android.support.test:runner:${Versions.espressoRunner}"
    val espressoRules = "com.android.support.test:rules:${Versions.espressoRules}"
    val espressoCore = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    val espressoWeb = "com.android.support.test.espresso:espresso-web:${Versions.espresso}"
    val espressoContrib = "com.android.support.test.espresso:espresso-contrib:${Versions.espresso}"
    val espressoIntents = "com.android.support.test.espresso:espresso-intents:${Versions.espresso}"
    val junit = "junit:junit:${Versions.junit}"

}