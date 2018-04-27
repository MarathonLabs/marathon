package com.malinskiy.marathon.android

import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File


class ApkParserSpek : Spek({
    describe("apk parser") {
        it("should parser AndroidManifest and return InstrumentationInfo") {
            val parser = ApkParser()
            val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
            parser.parseInstrumentationInfo(apkFile) shouldEqual InstrumentationInfo("com.example",
                    "com.example.test",
                    "android.support.test.runner.AndroidJUnitRunner"
            )
        }
    }
})