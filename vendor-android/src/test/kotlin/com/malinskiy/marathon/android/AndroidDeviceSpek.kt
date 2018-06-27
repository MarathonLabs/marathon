package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class AndroidDeviceSpek : Spek({
    describe("android device") {
        val iDevice = mock<IDevice>()
        whenever(iDevice.serialNumber).thenReturn("serial")
        it("model return Unknown if ddmDevice property ro.product.model") {
            whenever(iDevice.getProperty("ro.product.model")).thenReturn(null)
            AndroidDevice(iDevice).model shouldBe "Unknown"
        }
        it("manufacturer return Unknown if ddmlib property ") {
            whenever(iDevice.getProperty("ro.product.manufacturer")).thenReturn(null)
            AndroidDevice(iDevice).manufacturer shouldBe "Unknown"
        }
    }
})
