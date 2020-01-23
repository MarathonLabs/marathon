object Versions {
    val kotlin = "1.3.61"

    val androidGradleVersion = "3.5.2"

    val kakao = "2.2.0"
    val espresso = "3.2.0"
    val espressoRunner = "1.2.0"
    val espressoRules = "1.2.0"
    val appCompat = "1.1.0"
    val constraintLayout = "1.1.3"
    val allure = "2.0.0"

}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
}

object Libraries {
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
}

object TestLibraries {
    val kakao = "com.agoda.kakao:kakao:${Versions.kakao}"

    val espressoRunner = "androidx.test:runner:${Versions.espressoRunner}"
    val espressoRules = "androidx.test:rules:${Versions.espressoRules}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    val allureAndroidCommon = "io.qameta.allure:allure-android-commons:${Versions.allure}"
    val allureAndroidModel = "io.qameta.allure:allure-android-model:${Versions.allure}"
    val allureAndroidEspresso = "io.qameta.allure:allure-espresso:${Versions.allure}"
}
