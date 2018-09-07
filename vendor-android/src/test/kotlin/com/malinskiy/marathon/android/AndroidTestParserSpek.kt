package com.malinskiy.marathon.android

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

class AndroidTestParserSpek : Spek({
    describe("android test parser") {
        val parser = AndroidTestParser()

        on("android test apk") {
            val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)

            val configuration = Configuration("name", File(""), File(""), apkFile, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, AndroidConfiguration(File("")))

            it("should return proper list of test methods") {
                val extractedTests = parser.extract(configuration)
                extractedTests shouldEqual listOf(Test("com.example", "MainActivityTest", "testText",
                        listOf("org.junit.Test", "kotlin.Metadata", "org.junit.runner.RunWith")))
            }
        }
    }
})

