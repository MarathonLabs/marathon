package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ddmlib.DdmlibAndroidDevice
import com.malinskiy.marathon.android.serial.SerialStrategy
import com.malinskiy.marathon.time.SystemTimer
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Clock

class AndroidDeviceSpek : Spek(
    {
        describe("android device") {
            val iDevice = mock<IDevice>()
            whenever(iDevice.serialNumber).thenReturn("serial")
            val track = Track()
            val timer = SystemTimer(Clock.systemDefaultZone())

            it("model return Unknown if ddmDevice property ro.product.model") {
                whenever(iDevice.getProperty("ro.product.model")).thenReturn(null)
                DdmlibAndroidDevice(iDevice, track, timer, SerialStrategy.AUTOMATIC).model shouldBe "Unknown"
            }
            it("manufacturer return Unknown if ddmlib property ") {
                whenever(iDevice.getProperty("ro.product.manufacturer")).thenReturn(null)
                DdmlibAndroidDevice(iDevice, track, timer, SerialStrategy.AUTOMATIC).manufacturer shouldBe "Unknown"
            }
            it("should return ddmlib version instead of ro.build.version.sdk property value") {
                val default = AndroidVersion.DEFAULT
                whenever(iDevice.version).thenReturn(default)
                whenever(iDevice.getProperty("ro.build.version.sdk")).thenReturn("INVALID_VERSION")
                DdmlibAndroidDevice(iDevice, track, timer, SerialStrategy.AUTOMATIC).operatingSystem.version shouldBeEqualTo default.apiString
            }
        }
    })
