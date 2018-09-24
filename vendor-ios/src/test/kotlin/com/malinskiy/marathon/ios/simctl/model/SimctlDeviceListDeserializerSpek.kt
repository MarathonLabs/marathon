package com.malinskiy.marathon.ios.simctl.model

import com.google.gson.GsonBuilder
import com.malinskiy.marathon.test.Test
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

class SimctlDeviceListDeserializerSpek : Spek({
    describe("simctl list output deserializer") {
        val gson = GsonBuilder()
                .registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
                .create()

        on("sample json") {
            val sampleFile = File(javaClass.classLoader.getResource("fixtures/simctl/list_output.json").file)

            it("should deserialize properly") {
                val devicesList = gson.fromJson(sampleFile.reader(), SimctlListDevicesOutput::class.java)
                devicesList.devices.devices.size `should be equal to` 45
            }
        }
    }
})
