package com.malinskiy.marathon.gradle.task

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.configuration.BuildConfig
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File


class CachingTest {
    @Test
    fun testUnpackingIsCached() {
        val runner = GradleRunner.create()
            .withGradleVersion("7.3.3")
            .withProjectDir(testProjectDir)
            .withArguments(":marathonWrapper", "--build-cache", "--stacktrace")
        var result = runner.build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")

        result = runner
            .withArguments(":marathonWrapper", "--build-cache", "--stacktrace", "--info")
            .build()

        assertThat(result.task(":marathonWrapperExtract")!!.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
        assertThat(result.task(":marathonWrapper")!!.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun testMarathonfileGenerationIsCached() {
        val runner = GradleRunner.create()
            .withGradleVersion("7.3.3")
            .withProjectDir(testProjectDir)
            .withArguments(":app:marathonDebugAndroidTestGenerateMarathonfile", "--build-cache", "--stacktrace")
        var result = runner.build()

        assertThat(result.output).contains("BUILD SUCCESSFUL")

        result = runner
            .withArguments(":app:marathonDebugAndroidTestGenerateMarathonfile", "--build-cache", "--stacktrace")
            .build()

        assertThat(result.task(":app:marathonDebugAndroidTestGenerateMarathonfile")!!.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    companion object {
        @TempDir
        lateinit var testProjectDir: File
        @BeforeAll
        @JvmStatic
        internal fun setup() {
            File(testProjectDir, "local.properties").writeText("sdk.dir=${androidHome()}")
            val fixture = File(CachingTest::class.java.protectionDomain.classLoader.getResource("sample1").toURI())
            fixture.copyRecursively(overwrite = true, target = testProjectDir)

            val version = StringBuilder().apply {
                append(BuildConfig.VERSION)
                if (BuildConfig.RELEASE_MODE == "SNAPSHOT") {
                    append("-SNAPSHOT")
                }
            }.toString()
            File(testProjectDir, "build.gradle").apply {
                val content = readText()
                writeText(content.replaceFirst("\${MARATHON_VERSION}", version))
            }
        }

        private fun androidHome(): String {
            return System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
            ?: throw IllegalStateException("Missing Android SDK installation. Please specify 'ANDROID_SDK_ROOT'")
        }
    }
}
