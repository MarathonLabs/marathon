object Versions {
    val marathon = "0.1.5"

    val kotlin = "1.2.51"
    val coroutines = "0.21"

    val ddmlib = "26.1.1"
    val dexTestParser = "1.1.0"
    val kotlinLogging = "1.4.9"
    val axmlParser = "1.0"

    val junitGradle = "1.0.0"
    val androidGradleVersion = "3.0.1"

    val spek = "1.1.5"
    val kluent = "1.35"

    val kakao = "1.2.1"
    val espresso = "3.0.1"
    val espressoRules = "1.0.1"
    val espressoRunner = "1.0.1"
    val junit = "4.12"
    val gson = "2.8.5"
    val apacheCommonsText = "1.3"
    val apacheCommonsIO = "2.6"
    val influxDbClient = "2.10"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val junitGradle = "org.junit.platform:junit-platform-gradle-plugin:${Versions.junitGradle}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
}

object Libraries {
    val ddmlib = "com.android.tools.ddms:ddmlib:${Versions.ddmlib}"
    val dexTestParser = "com.linkedin.dextestparser:parser:${Versions.dexTestParser}"
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    val kotlinLogging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"
    val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val axmlParser = "com.shazam:axmlparser:${Versions.axmlParser}"
    val gson = "com.google.code.gson:gson:${Versions.gson}"
    val apacheCommonsText = "org.apache.commons:commons-text:${Versions.apacheCommonsText}"
    val apacheCommonsIO = "commons-io:commons-io:${Versions.apacheCommonsIO}"
    val influxDbClient = "org.influxdb:influxdb-java:${Versions.influxDbClient}"
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
