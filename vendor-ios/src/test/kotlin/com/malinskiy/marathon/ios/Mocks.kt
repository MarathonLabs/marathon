package com.malinskiy.marathon.ios

import com.google.gson.GsonBuilder
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.ios.cmd.remote.CommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.CommandResult
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceListDeserializer
import net.schmizz.sshj.connection.channel.direct.Session
import org.amshove.kluent.mock

class Mocks {
    class CommandExecutor {
        companion object {
            val DEFAULT = object : com.malinskiy.marathon.ios.cmd.remote.CommandExecutor {
                val mock = mock(Session::class)
                override fun startSession(): Session = mock
                override fun exec(command: String, timeout: Long): CommandResult {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }
    }

    class IOSDevice {
        companion object {
            private val gson = GsonBuilder().registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer()).create()

            val DEFAULT = com.malinskiy.marathon.ios.IOSDevice("63D0962A-0A41-4BE9-A99E-E6220412BEB1",
                    CommandExecutor.DEFAULT,
                    gson)
        }
    }

    class DevicePoolId {
        companion object {
            val DEFAULT = com.malinskiy.marathon.device.DevicePoolId("testpool")
        }
    }
}
