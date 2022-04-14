package com.malinskiy.marathon.android

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.io.File


class ApkParserTest {

    @Test
    fun `should extract and parse AndroidManifest and return InstrumentationInfo`() {
        val parser = ApkParser()
        val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
        parser.parseInstrumentationInfo(apkFile) shouldEqual InstrumentationInfo(
            "com.example",
            "com.example.test",
            "android.support.test.runner.AndroidJUnitRunner"
        )
    }

    @Test
    fun `should parse AndroidManifest 0`() {
        val parser = ApkParser()
        val file = File(javaClass.classLoader.getResource("manifest/manifest-0.xml").file)
        file.inputStream().use {
            parser.parseAndroidManifest(it) shouldBeEqualTo InstrumentationInfo(
                "com.example",
                "com.example.test",
                "android.support.test.runner.AndroidJUnitRunner"
            )
        }
    }
}
