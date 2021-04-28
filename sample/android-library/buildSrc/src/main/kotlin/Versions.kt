object Versions {
    val kotlin = "1.4.31"
    val coroutines = "1.3.9"

    val androidGradleVersion = "4.1.0"
    val espressoRunner = "1.3.0"
    val testJunit = "1.1.2"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
}

object Libraries {
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
}

object TestLibraries {
    val testRunner = "androidx.test:runner:${Versions.espressoRunner}"
    val extJunit = "androidx.test.ext:junit:${Versions.testJunit}"
}
