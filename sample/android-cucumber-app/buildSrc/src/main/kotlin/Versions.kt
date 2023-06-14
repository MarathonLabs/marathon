object Versions {
    val kotlin = "1.8.10"

    val androidGradleVersion = "7.4.1"

    val cucumber = "4.9.0"
    val cucumberPicocontainer = "4.8.1"
    val espresso = "3.5.1"
    val espressoRules = "1.5.0"
    val junit = "4.12"
    val appCompat = "1.6.0"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
}

object Libraries {
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
}

object TestLibraries {
    val cucumber = "io.cucumber:cucumber-android:${Versions.cucumber}"
    val cucumberPicocontainer = "io.cucumber:cucumber-picocontainer:${Versions.cucumberPicocontainer}"

    val testRules = "androidx.test:rules:${Versions.espressoRules}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
}
