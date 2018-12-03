package com.malinskiy.marathon.ios

import com.google.gson.GsonBuilder
import com.malinskiy.marathon.ios.cmd.remote.CommandResult
import com.malinskiy.marathon.ios.cmd.remote.CommandSession
import com.malinskiy.marathon.ios.device.RemoteSimulator
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceListDeserializer
import org.amshove.kluent.mock

class Mocks {
    class CommandExecutor {
        companion object {
            val DEFAULT = object : com.malinskiy.marathon.ios.cmd.remote.CommandExecutor {
                val mock = mock(CommandSession::class)
                override fun startSession(command: String, timeoutMillis: Long): CommandSession = mock
                override fun exec(command: String, testOutputTimeoutMillis: Long): CommandResult {
                    TODO("not implemented")
                }
                override suspend fun exec(command: String, testOutputTimeoutMillis: Long, onLine: (String) -> Unit): Int? {
                    TODO("not implemented")
                }
                override fun disconnect() {
                    TODO("not implemented")
                }
            }
        }
    }

    class IOSDevice {
        companion object {
            private val gson = GsonBuilder().registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer()).create()

            val DEFAULT = com.malinskiy.marathon.ios.IOSDevice(
                RemoteSimulator("localhost", "63D0962A-0A41-4BE9-A99E-E6220412BEB1", null),
                mock(IOSConfiguration::class),
                gson)
        }
    }

    class DevicePoolId {
        companion object {
            val DEFAULT = com.malinskiy.marathon.device.DevicePoolId("testpool")
        }
    }
}
