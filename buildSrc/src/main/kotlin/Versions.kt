object Versions {
    val marathon = System.getenv("GIT_TAG_NAME") ?: "0.10.1"

    val kotlin = "1.9.24"
    val coroutines = "1.8.1"
    val coroutinesTest = coroutines

    val androidCommon = "31.6.1"
    val adam = "0.5.8"
    val dexTestParser = "2.3.4"
    val kotlinLogging = "3.0.5"
    val logbackClassic = "1.5.8"
    val axmlParser = "1.0"
    val bugsnag = "3.7.2"

    val junitGradle = "1.2.0"

    val junit5 = "5.11.0"
    val kluent = "1.73"

    val kakao = "3.0.2"
    val gson = "2.11.0"
    val apacheCommonsText = "1.12.0"
    val apacheCommonsIO = "2.17.0"
    val okhttp = "4.12.0"
    val ktor = "2.3.6"
    val influxDbClient = "2.24"
    val influxDb2Client = "7.2.0"
    val clikt = "5.0.0"
    val jacksonDatabind = "2.17.2"
    val jacksonKotlin = jacksonDatabind
    val jacksonYaml = jacksonDatabind
    val jacksonJSR310 = jacksonDatabind
    val jacksonAnnotations = jacksonDatabind
    val ddPlist = "1.28"
    val guava = "33.3.0-jre"
    val rsync4j = "3.3.0-1"
    val sshj = "0.39.0"
    val kotlinProcess = "1.5"
    val testContainers = "1.20.1"
    val jupiterEngine = junit5
    val jansi = "2.4.1"
    val scalr = "4.2"
    val allureTestFilter = "2.29.0"
    val allureJava = "2.29.0"
    val allureKotlin = "2.4.0"
    val allureEnvironment = "1.0.0"
    val mockitoKotlin = "5.4.0"
    val dokka = "1.9.10"
    val koin = "3.5.6"
    val jsonAssert = "1.5.3"
    val xmlUnit = "2.10.0"
    val assertk = "0.28.1"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val junitGradle = "org.junit.platform:junit-platform-gradle-plugin:${Versions.junitGradle}"
    val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
}

object Libraries {
    val adam = "com.malinskiy.adam:adam:${Versions.adam}"
    val adamTestrunnerContract = "com.malinskiy.adam:android-testrunner-contract:${Versions.adam}"
    val androidCommon = "com.android.tools:common:${Versions.androidCommon}"
    val dexTestParser = "com.linkedin.dextestparser:parser:${Versions.dexTestParser}"
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinStdLibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    val kotlinLogging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"
    val logbackClassic = "ch.qos.logback:logback-classic:${Versions.logbackClassic}"
    val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val axmlParser = "com.shazam:axmlparser:${Versions.axmlParser}"
    val gson = "com.google.code.gson:gson:${Versions.gson}"
    val apacheCommonsText = "org.apache.commons:commons-text:${Versions.apacheCommonsText}"
    val apacheCommonsIO = "commons-io:commons-io:${Versions.apacheCommonsIO}"
    val influxDbClient = "org.influxdb:influxdb-java:${Versions.influxDbClient}"
    val influxDb2Client = "com.influxdb:influxdb-client-java:${Versions.influxDb2Client}"
    val clikt = "com.github.ajalt.clikt:clikt:${Versions.clikt}"
    val jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}"
    val jacksonAnnotations = "com.fasterxml.jackson.core:jackson-annotations:${Versions.jacksonAnnotations}"
    val jacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonKotlin}"
    val jacksonYaml = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jacksonYaml}"
    val jacksonJSR310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jacksonJSR310}"
    val ddPlist = "com.googlecode.plist:dd-plist:${Versions.ddPlist}"
    val guava = "com.google.guava:guava:${Versions.guava}"
    val rsync4j = "com.github.fracpete:rsync4j-all:${Versions.rsync4j}"
    val sshj = "com.hierynomus:sshj:${Versions.sshj}"
    val jansi = "org.fusesource.jansi:jansi:${Versions.jansi}"
    val scalr = "org.imgscalr:imgscalr-lib:${Versions.scalr}"
    val allure = "io.qameta.allure:allure-java-commons:${Versions.allureJava}"
    val allureEnvironment = "com.github.automatedowl:allure-environment-writer:${Versions.allureEnvironment}"
    val allureKotlinCommons = "io.qameta.allure:allure-kotlin-commons:${Versions.allureKotlin}"
    val allureTestFilter = "io.qameta.allure:allure-test-filter:${Versions.allureTestFilter}"
    val koin = "io.insert-koin:koin-core:${Versions.koin}"
    val bugsnag = "com.bugsnag:bugsnag:${Versions.bugsnag}"
    val kotlinProcess = "com.github.pgreze:kotlin-process:${Versions.kotlinProcess}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val ktorNetwork = "io.ktor:ktor-network:${Versions.ktor}"
}

object TestLibraries {
    val junit5 = "org.junit.jupiter:junit-jupiter:${Versions.junit5}"
    val kluent = "org.amshove.kluent:kluent:${Versions.kluent}"

    val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}"
    val jupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.jupiterEngine}"
    val koin = "io.insert-koin:koin-test:${Versions.koin}"
    val jsonAssert = "org.skyscreamer:jsonassert:${Versions.jsonAssert}"
    val xmlUnit = "org.xmlunit:xmlunit-matchers:${Versions.xmlUnit}"
    val assertk = "com.willowtreeapps.assertk:assertk:${Versions.assertk}"
    val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesTest}"

    val testContainers = "org.testcontainers:testcontainers:${Versions.testContainers}"
    val testContainersJupiter = "org.testcontainers:junit-jupiter:${Versions.testContainers}"
    val testContainersInflux = "org.testcontainers:influxdb:${Versions.testContainers}"
    val adamServerStubJunit5 = "com.malinskiy.adam:server-stub-junit5:${Versions.adam}"
}
