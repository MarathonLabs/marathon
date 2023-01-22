package com.malinskiy.marathon.ios.xcresulttool

import com.malinskiy.marathon.ios.bin.xcrun.xcresulttool.Xcresulttool.Companion.AppleJsonMapper
import com.malinskiy.marathon.vendor.ios.xcrun.xcresulttool.ActionsInvocationRecord
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.jupiter.api.Test
import java.io.File

class XcresulttoolTest {
    
    @Test
    fun testSample1() {
        val json = File(javaClass.classLoader.getResource("fixtures/xctesttool/sample_1.json").file).readText()
        val record = AppleJsonMapper.readValue(json, ActionsInvocationRecord::class.java)
        record.issues.testFailureSummaries.size shouldBeGreaterThan 0
    }

    @Test
    fun testSample2() {
        val json = File(javaClass.classLoader.getResource("fixtures/xctesttool/sample_2.json").file).readText()
        val record = AppleJsonMapper.readValue(json, ActionsInvocationRecord::class.java)
        record.issues.testFailureSummaries.size shouldBeEqualTo 0
    }
}
