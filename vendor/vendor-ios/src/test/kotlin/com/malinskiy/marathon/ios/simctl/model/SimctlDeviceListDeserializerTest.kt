package com.malinskiy.marathon.ios.simctl.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.io.File

class SimctlDeviceListDeserializerTest {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
        .create()
    private val sampleFile =
        File(javaClass.classLoader.getResource("fixtures/simctl/list_output.json").file)

    @Test
    fun `should deserialize properly`() {
        val devicesList =
            gson.fromJson(sampleFile.reader(), SimctlListDevicesOutput::class.java)
        devicesList.devices.devices.size `should be equal to` 45
    }
}
