package com.malinskiy.marathon.ios.xcresulttool

import com.google.gson.GsonBuilder
import com.malinskiy.marathon.ios.xcrun.xcresulttool.AppleJsonTypeAdapterFactory
import com.malinskiy.marathon.ios.xcrun.xcresulttool.AppleListConverter
import com.malinskiy.marathon.vendor.ios.xcrun.xcresulttool.ActionsInvocationRecord
import org.junit.jupiter.api.Test
import java.io.File

class XcresulttoolTest {
    /**
     * For the Apple trickery with json we require a custom deserializer
     */
    private val gson = GsonBuilder()
        .registerTypeAdapter(List::class.java, AppleListConverter())
        .registerTypeAdapterFactory(AppleJsonTypeAdapterFactory())
        .create()
    @Test
    fun testSample1() {
        val json = File(javaClass.classLoader.getResource("fixtures/xctesttool/sample_1.json").file).readText()
        val record = gson.fromJson(json, ActionsInvocationRecord::class.java)
        record.issues.testFailureSummaries.forEach { println(it) }
    }
}
