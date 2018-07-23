package com.malinskiy.marathon.android

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.withMessage
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

            it("should return proper list of test methods") {
                val extractedTests = parser.extract(apkFile, emptyList())
                extractedTests shouldEqual listOf(Test("com.example", "MainActivityTest", "testText",
                        listOf("org.junit.Test", "kotlin.Metadata", "org.junit.runner.RunWith")))
            }
            it("should ignore test by class name using filter") {
                val func = { parser.extract(apkFile, listOf("Ignore".toRegex())) }
                func shouldThrow NoTestCasesFoundException::class withMessage "No tests cases were found in the test APK: ${apkFile.absolutePath}"
            }
        }
    }
})

