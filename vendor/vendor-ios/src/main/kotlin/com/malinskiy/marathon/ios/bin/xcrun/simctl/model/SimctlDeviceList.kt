package com.malinskiy.marathon.ios.bin.xcrun.simctl.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

data class SimctlDeviceList(val devices: List<SimctlDevice>)


class SimctlDeviceListDeserializer : JsonDeserializer<SimctlDeviceList> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SimctlDeviceList {
        val devices = mutableListOf<SimctlDevice>()

        val jsonObject = json?.asJsonObject
        jsonObject?.entrySet()?.forEach {
            val runtime = it.key
            val devicesJsonArray = it.value?.asJsonArray

            devicesJsonArray?.forEach {
                val deviceJson = it.asJsonObject
                val stateJson = deviceJson?.get("state")
                val name = deviceJson?.get("name")?.asString
                val udid = deviceJson?.get("udid")?.asString
                val logPath = deviceJson?.get("logPath")?.asString
                val isAvailable = deviceJson?.get("isAvailable")?.asBoolean
                val deviceTypeIdentifier = deviceJson?.get("deviceTypeIdentifier")?.asString

                if (stateJson != null && context != null && name != null && udid != null) {
                    val state: SimctlDevice.State = try {
                        context.deserialize(stateJson, SimctlDevice.State::class.java) ?: SimctlDevice.State.Unknown
                    } catch (e: JsonSyntaxException) {
                        SimctlDevice.State.Unknown
                    }

                    devices.add(
                        SimctlDevice(runtime, state, name, udid, logPath, isAvailable, null, null,
                                     deviceTypeIdentifier)
                    )
                }
            }
        }

        return SimctlDeviceList(devices)
    }

}
