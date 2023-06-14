package com.malinskiy.marathon.android

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.io.File


class ApkParserTest {

    @Test
    fun `should parser AndroidManifest and return InstrumentationInfo`() {
        val parser = ApkParser()
        val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
        parser.parseInstrumentationInfo(apkFile) shouldBeEqualTo InstrumentationInfo(
            "com.example",
            "com.example.test",
            "android.support.test.runner.AndroidJUnitRunner"
        )
    }
}
