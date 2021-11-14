object Versions {
    val kotlin = "1.5.21"
    val coroutines = "1.5.2"

    val androidGradleVersion = "7.0.3"

    val kakao = "3.0.2"
    val espresso = "3.3.0"
    val espressoRules = "1.3.0"
    val espressoRunner = "1.3.0"
    val testJunit = "1.1.2"
    val junit = "4.13.2"
    val appCompat = "1.2.0"
    val constraintLayout = "2.0.4"
    val allure = "2.1.2"
    val adam = "0.4.3"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
}

object Libraries {
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
}

object TestLibraries {
    val kakao = "io.github.kakaocup:kakao:${Versions.kakao}"
    val adamJunit4 = "com.malinskiy.adam:android-junit4:${Versions.adam}"
    val testRunner = "androidx.test:runner:${Versions.espressoRunner}"
    val testRules = "androidx.test:rules:${Versions.espressoRules}"
    val extJunit = "androidx.test.ext:junit:${Versions.testJunit}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    val allureKotlinModel = "io.qameta.allure:allure-kotlin-model:${Versions.allure}"
    val allureKotlinCommon = "io.qameta.allure:allure-kotlin-commons:${Versions.allure}"
    val allureKotlinJunit4 = "io.qameta.allure:allure-kotlin-junit4:${Versions.allure}"
    val allureKotlinAndroid = "io.qameta.allure:allure-kotlin-android:${Versions.allure}"

    val testOutputEnhancer = "com.malinskiy.adam:android-junit4-test-annotation-producer:${Versions.adam}"
}
