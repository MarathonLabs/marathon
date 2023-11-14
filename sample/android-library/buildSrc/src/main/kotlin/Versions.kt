object Versions {
    val kotlin = "1.9.10"
    val coroutines = "1.7.3"
    val androidGradleVersion = "8.1.3"
    val espressoRunner = "1.5.2"
    val testJunit = "1.1.5"
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
