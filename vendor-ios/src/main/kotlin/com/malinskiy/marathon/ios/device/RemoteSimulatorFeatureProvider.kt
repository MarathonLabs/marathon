package com.malinskiy.marathon.ios.device

import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.ios.IOSDevice

object RemoteSimulatorFeatureProvider {
    fun deviceFeatures(device: IOSDevice): Collection<DeviceFeature> {
        return enumValues<DeviceFeature>().filter { featureIsAvailable(device, it) }
    }

    private fun featureIsAvailable(device: IOSDevice, feature: DeviceFeature): Boolean {
        return when (feature) {
            DeviceFeature.SCREENSHOT -> true
            DeviceFeature.VIDEO -> {
                val session = device.hostCommandExecutor.startSession()
                val command = session.exec(
                        "/usr/sbin/system_profiler -detailLevel mini -xml SPDisplaysDataType"
                )
                command.join()

                return command.inputStream.bufferedReader().readLines()
                        .any { it.contains("spdisplays_metalfeatureset") }
                        .also { session.close() }
            }
        }
    }

    fun availablePort(device: IOSDevice): Int {
        val commandResult = device.hostCommandExecutor.exec(
                """ruby -e 'require "socket"; puts Addrinfo.tcp("", 0).bind {|s| s.local_address.ip_port }'"""
        )
        return when {
            commandResult.exitStatus == 0 -> commandResult.stdout.toIntOrNull()
            else -> null
        } ?: throw Exception(commandResult.stderr)
    }
}
